
import functools
import itertools
import operator


class FactorException(Exception):
    """Exception for manipulation with factors."""
    pass


class Variable:
    """
    A variable defined by it's name and list of values.
    Variables are fully distinguished by their names, hence there cannot
    be two different variables of the same in common factor.
    """
    def __init__(self, name, values):
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

    def __init__(self, uncond_vars, cond_vars, prob):
        assert(len(uncond_vars) > 0)
        all_vars = uncond_vars + cond_vars
        card = Factor._card(all_vars)
        assert(product(card) == len(prob))
        # make it tuples instead of lists => enforce immutability
        self.uncond_vars = tuple(uncond_vars)
        self.cond_vars = tuple(cond_vars)
        self.all_vars = tuple(all_vars)
        self.card = card  # list of cardinalities for each variable
        self.prob = tuple(prob)
    
    def multiply(self, f2):
        """Multiply with factor f2 and return result as a new factor."""
        f1 = self
        if not set(f1.uncond_vars).isdisjoint(set(f2.uncond_vars)):
            # TODO instead should enforce somethink like: P(A,B) * P(B,C), where A union C is non-empty
            #      (ie. variables of f1 aren't subset of variables of f2 and vice versa)
            raise FactorException("multiplying something like P(X, ...|...) * P(X, ...|)")
        if f2.cond_vars:
            raise FactorException("right operand may not have any conditional variable")
            
        res_uncond_vars = Factor._variables_union(f1.uncond_vars, f2.uncond_vars)
        res_cond_vars = Factor._variables_difference(f1.cond_vars, f2.uncond_vars)
        res_all_vars = res_uncond_vars + res_cond_vars
        res_card = Factor._card(res_all_vars)
        res_prob = [0 for i in range(product(res_card))]
        # generate every assignment for the resulting factor
        for (assignment_res, assignment_f1, assignment_f2) in Factor._multiplication_assignment_generator(res_all_vars, f1.all_vars, f2.all_vars):
            prob_f1 = f1.prob[Factor._map_assignment_to_index(assignment_f1, f1.card)]
            prob_f2 = f2.prob[Factor._map_assignment_to_index(assignment_f2, f2.card)]
            res_prob[Factor._map_assignment_to_index(assignment_res, res_card)] = prob_f1 * prob_f2
        
        return Factor(res_uncond_vars, res_cond_vars, res_prob)
    
    # TODO def __mul__
    
    def marginalize(self, vars_to_marginalize):
        """Marginalize over specified variables."""
        # TODO nefunguje spravne pro faktory s podminkovymi promennymi (lze to vubec??)
        if Factor._variables_intersection(self.cond_vars, vars_to_marginalize):
            raise FactorException("Don't know how to marginalize over a condition variable")
        def remap_values(values, indices):
            remaped = [None for i in range(len(values))]
            for (v, i) in zip(values, indices):
                remaped[i] = v
            return remaped
        res_uncond_vars = Factor._variables_difference(self.uncond_vars, vars_to_marginalize)
        res_cond_vars = Factor._variables_difference(self.cond_vars, vars_to_marginalize)
        res_all_vars = res_uncond_vars + res_cond_vars
        res_card = Factor._card(res_all_vars)
        res_prob = [0 for i in range(product(res_card))]
        
        res_mapping = tuple(self.all_vars.index(rv) for rv in res_all_vars)  # on which indices do variables of result map to original variables
        marginalize_mapping = tuple(self.all_vars.index(mv) for mv in vars_to_marginalize)
        
        for res_assignment in Factor._assignment_generator(res_all_vars):
            summed_prob = 0
            for marginalize_assignment in Factor._assignment_generator(vars_to_marginalize):
                orig_assignment = remap_values(res_assignment + marginalize_assignment, res_mapping + marginalize_mapping)
                orig_index = Factor._map_assignment_to_index(orig_assignment, self.card)
                summed_prob += self.prob[orig_index]
            res_assignment_index = Factor._map_assignment_to_index(res_assignment, res_card)
            res_prob[res_assignment_index] = summed_prob
        
        return Factor(res_uncond_vars, res_cond_vars, res_prob)
    
    def reduce(self, evidences_dict):
        # TODO set all probabilities inconsistent with evidence to zero
        pass
    
    def renormalize(self):
        """
        Make possibly unnormalized factor normalized.
        Logic is as follows: For every case of evidence (ie. for every full assignment
        of condition variables) all possibile assignments of unconditional
        (evidence) variables must sum to one.
        Let the X1, ..., Xn be unconditional variables. It means each block
        of |X1| * ... * |Xn| values has to sum to 1 because after this may values
        the assignment of condition variables changes. Note that if there is no
        condition variable, the whole table must sum to 1.
        """
        renorm_period = product(Factor._card(self.uncond_vars))  # aka block size
        renorm_prob = list(self.prob)
        renorm_block_count = len(self.prob) // renorm_period
        for renorm_block in range(renorm_block_count):
            begin = renorm_block * renorm_period
            end = begin + renorm_period
            block_sum = sum(self.prob[begin:end])
            if not double_eq(block_sum, 1.0):
                for i in range(begin, end):
                    renorm_prob[i] /= block_sum
        return Factor(self.uncond_vars, self.cond_vars, renorm_prob)
    
    def probability(self, evidences_dict):
        # TODO
        pass
    
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
    def _multiplication_assignment_generator(res_vars, f1_vars, f2_vars):
        """
        f1_vars and f2_vars are subsets of res_vars. This generator generates
        every possible assignment for res_vars and additionaly extracts
        current assignment for variables in f1_vars and f2_vars. Finally
        it yields triples of (assignment_result, assignment_f1, assignment_f2)."""
        def filter_assignment_by_mask(assignment, mask):
            reduced_assignment = []
            for (mask_index, mask_bit) in enumerate(mask):
                if mask_bit:
                    reduced_assignment.append(assignment[mask_index])
            return reduced_assignment
        f1_mask = [((res_v in f1_vars and 1 or 0)) for res_v in res_vars]  # bit vector (1 on i-th position ~ i-th variable of res_vars is in f1_vars)
        f2_mask = [((res_v in f2_vars and 1 or 0)) for res_v in res_vars]
        
        for res_assignment in Factor._assignment_generator(res_vars):
            f1_assignment = filter_assignment_by_mask(res_assignment, f1_mask)
            f2_assignment = filter_assignment_by_mask(res_assignment, f2_mask)
            yield (res_assignment, f1_assignment, f2_assignment)
    
    @staticmethod
    def _map_assignment_to_index(assignment, card):
        assignment = tuple(assignment)
        assert(len(assignment) == len(card))
        assert(assignment < card)
        index = 0
        multiplier = 1
        for i in range(len(assignment)):
            index += assignment[i] * multiplier
            multiplier *= card[i]
        return index
    
    @staticmethod
    def _map_index_to_assignment(index, card):
        assert(index < product(card))
        assignment = []
        for i in range(len(card)):
            assignment_i = index % card[i]
            assignment.append(assignment_i)
            index //= card[i]
        return assignment
    
    def __str__(self):
        MIN_COLUMN_WIDTH = 5
        title = "table for P(%s | %s):" % (", ".join(v.name for v in self.uncond_vars), ", ".join(v.name for v in self.cond_vars))
        
        header_items = [v.name for v in self.all_vars] + ["prob"]
        COLUMN_WIDTH = max(MIN_COLUMN_WIDTH, max(len(name) for name in header_items))
        
        format_str = "^%ds" % COLUMN_WIDTH
        format_float = "^%df" % COLUMN_WIDTH
        header = " | ".join((format(hi, format_str)) for hi in header_items)
        separator = "=|=".join("=" * COLUMN_WIDTH for i in header_items)

        body_lines = []
        for i in range(len(self.prob)):
            p = self.prob[i]
            assignment = Factor._map_index_to_assignment(i, self.card)
            assignment_str_list = [self.all_vars[var_index].values[var_value] for (var_index, var_value) in enumerate(assignment)]
            body_line_items = [format(var_value, format_str) for var_value in assignment_str_list] + [format(p, format_float)]
            body_line = " | ".join(body_line_items)
            body_lines.append(body_line)
        return "\n".join([title, header, separator] + body_lines)


class UnconditionalFactor(Factor):
    pass


class ConditionalFactor(Factor):
    pass



def product(iterable):
    """Return product of the whole iterable. Return 1 for empty iterable."""
    return functools.reduce(operator.mul, iterable, 1)

def double_eq(v1, v2):
    eps = 1e-5
    return abs(v1 - v2) <= eps

if __name__ == "__main__":
    var_rain = Variable("Rain", ["r0", "r1"])
    var_sprinkler = Variable("Sprinkler", ["s0", "s1"])
    var_wet = Variable("Wet", ["w0", "w1"])
    
    factor_rain = Factor([var_rain], [], [0.9, 0.1])
    factor_sprinkler = Factor([var_sprinkler], [], [0.4, 0.6])
    factor_wet = Factor([var_wet], [var_rain, var_sprinkler], [0.99, 0.01, 0.3, 0.7, 0.3, 0.7, 0.02, 0.98])
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
    