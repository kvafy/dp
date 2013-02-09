
import functools
import itertools
import operator


class FactorException(Exception):
    """Exception for manipulation with factors."""
    pass


class Variable:
    """
    A variable defined by it's name and list of values.
    Values can be of any type as long as they are not duplicate. If some value,
    however, is of type 'int', it's index in values list has to be equal to
    it's numeric value.
    Variables are fully distinguished by their names, hence there cannot
    be two different variables of the same name in a factor.
    """
    def __init__(self, name, values):
        if len(values) != len(set(values)):
            raise Exception("Values of a variable have to be unique")  # TODO exception class
        if any(isinstance(v, int) and v != values.index(v) for v in values):
            raise Exception("Value of type int has incorrect position")  # TODO exception class
        self.name = name
        self.values = values  # list of strings (eg. ["0", "1"], ["rain0", "rain1"], ...)
    
    def __eq__(self, other):
        return self.name == other.name
    
    def __str__(self):
        return "<variable \"%s\" from {%s}>" % (self.name, ", ".join(self.values))
    
    def __hash__(self):
        return hash(self.name)


class Factor:
    """Immutable."""

    def __init__(self, scope, prob):
        if len(scope) == 0:
            raise FactorException("Factor must have non-empty scope")
        if len(scope) != len(set(scope)):
            raise FactorException("Variables in the scope of a factor may not be duplicate")
        card = Factor._card(scope)
        if product(card) != len(prob):
            raise FactorException("Invalid length of prob vector")
        if not all(p >= 0 for p in prob):
            raise FactorException("Value of a factor must be non-negative")
        # make it tuples instead of lists => enforce immutability
        self.scope = tuple(scope)
        self.card = card  # list of cardinalities for each variable
        self.prob = tuple(prob)
    
    def value(self, assignment_dict):
        """Get value of this factor for a particular assignment of variables."""
        def variable_value_to_index(var):
            assignment = assignment_dict[var]
            if not isinstance(assignment, int):
                assignment = var.values.index(assignment)
            return assignment
        try:
            factor_assignment = tuple(variable_value_to_index(var) for var in self.scope)
            index = Factor._map_assignment_to_index(factor_assignment, self.card)
            return self.prob[index]
        except KeyError:  # some variable from the scope of this factor hasn't assignment
            raise FactorException("Assignment is incomplete for this factor")
    
    def multiply(self, f2):
        """Multiply with factor f2 and return result as a new factor."""
        f1 = self
        res_scope = Factor._variables_union(f1.scope, f2.scope)
        res_card = Factor._card(res_scope)
        res_prob = [0 for i in range(product(res_card))]
        # generate every assignment for the resulting factor
        for (assignment_res, assignment_f1, assignment_f2) in Factor._multiple_assignment_generator(res_scope, f1.scope, f2.scope):
            prob_f1 = f1.prob[Factor._map_assignment_to_index(assignment_f1, f1.card)]
            prob_f2 = f2.prob[Factor._map_assignment_to_index(assignment_f2, f2.card)]
            res_prob[Factor._map_assignment_to_index(assignment_res, res_card)] = prob_f1 * prob_f2
        
        return Factor(res_scope, res_prob)
    
    def __mul__(self, f2):
        if isinstance(f2, Factor):
            return self.multiply(f2)
        # TODO makes sense for numeric types (eg. for renormalization)
        # docs: http://docs.python.org/3.3/reference/datamodel.html?highlight=object#object.__rmul__
        else:
            raise NotImplemented
    
    def marginalize(self, vars_to_marginalize):
        """Marginalize over specified variables."""
        res_scope = Factor._variables_difference(self.scope, vars_to_marginalize)
        res_card = Factor._card(res_scope)
        res_prob = [0 for i in range(product(res_card))]
        
        for (orig_assignment, res_assignment) in Factor._multiple_assignment_generator(self.scope, res_scope):
            orig_index = Factor._map_assignment_to_index(orig_assignment, self.card)
            res_index = Factor._map_assignment_to_index(res_assignment, res_card)
            res_prob[res_index] += self.prob[orig_index]
        
        return Factor(res_scope, res_prob)
    
    def reduce(self, evidences_dict):
        """Set all probabilities inconsistent with evidence to zero."""
        # TODO more efficiently
        def contradicts_evidence(assignment):
            for (e_var, e_val) in evidences_dict.items():
                try:
                    index = self.scope.index(e_var)
                    if assignment[index] != e_val:
                        return True
                except ValueError:
                    pass
            return False
        res_prob = list(self.prob)
        for assignment in Factor._assignment_generator(self.scope):
            if contradicts_evidence(assignment):
                index = Factor._map_assignment_to_index(assignment, self.card)
                res_prob[index] = 0
        return Factor(self.scope, res_prob)
    
    def renormalize(self):
        """Make the factor sum to one."""
        current_sum = sum(self.prob)
        if current_sum == 1:
            return self
        else:
            renorm_prob = [self.prob[i] / current_sum for i in range(len(self.prob))]
            return Factor(self.scope, renorm_prob)
    
    
    @staticmethod
    def _card(variables):
        """Generate tuple of cardinalities for given variables."""
        return tuple(len(v.values) for v in variables)
    
    @staticmethod
    def _variables_union(vars_a, vars_b):
        """
        Union of two lists of variables (vars_a U vars_b).
        Variables from A are before variables from B and their relative order stays the same.
        """
        return vars_a + tuple(vb for vb in vars_b if vb not in vars_a)
    
    @staticmethod
    def _variables_intersection(vars_a, vars_b):
        """
        Intersection of two lists of variables (vars_a A vars_b).
        Variables from A are before variables from B and their relative order stays the same.
        """
        return tuple(va for va in vars_a if va in vars_b)
    
    @staticmethod
    def _variables_difference(vars_a, vars_b):
        """
        Difference of two lists of variables (vars_a - vars_b).
        Relative order of variables stays the same.
        """
        return tuple(va for va in vars_a if va not in vars_b)
    
    def _variables_issubset(sup, sub):
        """Check if set of variables sub is a subset of set sup."""
        return all(v in sup for v in sub)
    
    @staticmethod
    def _assignment_generator(vars):
        """
        Generate one by one every possible assignment for given variables as
        a list of numbers 0..(#values-1), ie. not actual values.
        The leftmost variable value changes the most rapidly.
        """
        card = Factor._card(vars)
        assignment = [0 for i in range(len(vars))]
        while True:
            yield assignment
            i = 0
            while i < len(vars) and assignment[i] + 1 == card[i]:
                assignment[i] = 0
                i += 1
            if i == len(vars):
                break
            assignment[i] += 1
    
    @staticmethod
    def _multiple_assignment_generator(base_scope, *subscopes):
        """
        Generates all assignments for variables base_scope, one by one.
        Then there is additional list of subsets of base_scope and for
        the current assignment of base_scope it also generates assignments
        for variables in the subsets. Each combination is yielded as
        a tuple of 1 + len(subscopes) assignments to respective sets
        of variables, ie. (base_assignment, subscope1_assignment, ...).
        Note: Each subscope must be subset of base_scope.
        """
        if not all(Factor._variables_issubset(base_scope, ss) for ss in subscopes):  # TODO performance
            raise FactorException("variables of each subscope must be all in base_scope")
        def extract_assignment(src_assignment, mapping):
            return tuple(src_assignment[i] for i in mapping)
        # mappings[i][v] ~ what is the index of v-th variable from subscope "i"
        #                  in the base_scope list
        mappings = [[base_scope.index(sub_var) for sub_var in subscope] for subscope in subscopes]
        
        for base_assignment in Factor._assignment_generator(base_scope):
            subscopes_assignments = tuple(extract_assignment(base_assignment, mapping) for mapping in mappings)
            yield (base_assignment, ) + subscopes_assignments
    
    @staticmethod
    def _map_assignment_to_index(assignment, card):
        assignment = tuple(assignment)
        assert(len(assignment) == len(card))  # TODO performance
        assert(assignment < card)  # TODO performance
        index = 0
        multiplier = 1
        for i in range(len(assignment)):
            index += assignment[i] * multiplier
            multiplier *= card[i]
        return index
    
    @staticmethod
    def _map_index_to_assignment(index, card):
        assert(index < product(card))  # TODO performance
        assignment = []
        for i in range(len(card)):
            assignment_i = index % card[i]
            assignment.append(assignment_i)
            index //= card[i]
        return assignment
    
    def __str__(self):
        MIN_COLUMN_WIDTH = 5
        title = "table representation of factor(%s):" % (", ".join(v.name for v in self.scope), )
        
        header_items = [v.name for v in self.scope] + ["prob"]
        COLUMN_WIDTH = max(MIN_COLUMN_WIDTH, max(len(name) for name in header_items))
        
        format_str = "^%ds" % COLUMN_WIDTH
        format_float = "^%df" % COLUMN_WIDTH
        header = " | ".join((format(hi, format_str)) for hi in header_items)
        separator = "=|=".join("=" * COLUMN_WIDTH for i in header_items)

        body_lines = []
        for i in range(len(self.prob)):
            p = self.prob[i]
            assignment = Factor._map_index_to_assignment(i, self.card)
            assignment_str_list = [self.scope[var_index].values[var_value] for (var_index, var_value) in enumerate(assignment)]
            body_line_items = [format(var_value, format_str) for var_value in assignment_str_list] + [format(p, format_float)]
            body_line = " | ".join(body_line_items)
            body_lines.append(body_line)
        return "\n".join([title, header, separator] + body_lines)


def product(iterable):
    """Return product of the whole iterable. Return 1 for empty iterable."""
    return functools.reduce(operator.mul, iterable, 1)


if __name__ == "__main__":
    var_rain = Variable("Rain", ["r0", "r1"])
    var_sprinkler = Variable("Sprinkler", ["s0", "s1"])
    var_wet = Variable("Wet", ["w0", "w1"])
    
    factor_rain = Factor([var_rain], [0.9, 0.1])
    factor_sprinkler = Factor([var_sprinkler], [0.4, 0.6])
    factor_wet = Factor([var_wet, var_rain, var_sprinkler], [0.99, 0.01, 0.3, 0.7, 0.3, 0.7, 0.02, 0.98])
    print(factor_rain)
    print()
    print(factor_sprinkler)
    print()
    print(factor_wet)
    print()
    
    factor_r_s = factor_rain.multiply(factor_sprinkler)
    print(factor_r_s)
    print()
    factor_w_r_s = factor_wet.multiply(factor_r_s)
    print(factor_w_r_s)
    print()
    print(factor_w_r_s.renormalize())
    print()
    print(factor_w_r_s.marginalize([var_wet, var_rain]))
    