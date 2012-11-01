
import functools
import itertools
import operator


class Variable:
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

    def __init__(self, vars, cond_vars, prob):
        assert(len(vars) > 0)
        all_vars = vars + cond_vars
        card = [len(var.values) for var in all_vars] 
        assert(product(card) == len(prob))
        # make it tuples instead of lists => enforce immutability
        self.uncond_vars = tuple(vars)
        self.cond_vars = tuple(cond_vars)
        self.all_vars = tuple(all_vars)
        self.card = tuple(card)  # list of cardinalities for each variable
        self.prob = tuple(prob)
    
    def probability(self, assignment_dict):
        pass
    
    def marginalize(self, variables):
        """Marginalize over specified variables."""
        pass
    
    @staticmethod
    def _variables_union(vars_a, vars_b):
        """
        Union of two lists of variables (vars_a U vars_b).
        Variables from A are before variables from B and their relative order stays the same.
        """
        ret = list(vars_a) + [vb for vb in vars_b if vb not in vars_a]
        return tuple(ret)
    
    @staticmethod
    def _variables_intersection(vars_a, vars_b):
        """
        Intersection of two lists of variables (vars_a A vars_b).
        Variables from A are before variables from B and their relative order stays the same.
        """
        ret = [va for va in vars_a if va in vars_b]
    
    @staticmethod
    def _variables_difference(vars_a, vars_b):
        """
        Difference of two lists of variables (vars_a - vars_b).
        Relative order of variables stays the same.
        """
        ret = [va for va in vars_a if va not in vars_b]
        return tuple(ret)
    
    @staticmethod
    def _assignment_generator(card):
        # TODO more effectively
        if len(card) > 1:
            for ass_rest in Factor._assignment_generator(card[1:]):
                for ass_prefix in range(card[0]):
                    yield [ass_prefix] + ass_rest
        else:
            for ass_prefix in range(card[0]):
                yield [ass_prefix]
    
    def multiply(self, f2):
        """Multiply with factor f2 and return result as a new factor."""
        def assignment_by_mask(assignment, mask):
            reduced_assignment = []
            for (mask_index, mask_bit) in enumerate(mask):
                if mask_bit:
                    reduced_assignment.append(assignment[mask_index])
            return reduced_assignment
        f1 = self
        if not set(f1.uncond_vars).isdisjoint(set(f2.uncond_vars)):
            print([str(v) for v in f1.uncond_vars])
            print([str(v) for v in f2.uncond_vars])
            raise Exception("multiplying something like P(X, ...|...) * P(X, ...|...)")
        if f2.cond_vars:
            raise Exception("right operator may not have any conditional variable")
        res_uncond_vars = Factor._variables_union(f1.uncond_vars, f2.uncond_vars)
        res_cond_vars = Factor._variables_difference(f1.cond_vars, f2.uncond_vars)
        res_all_vars = res_uncond_vars + res_cond_vars
        res_card = tuple(len(v.values) for v in res_all_vars)
        f1_mask = [((res_v in f1.all_vars and 1 or 0)) for res_v in res_all_vars]  # bit vector (1 on i-th position ~ i-th variable of res_all_vars is in f1)
        f2_mask = [((res_v in f2.all_vars and 1 or 0)) for res_v in res_all_vars]
        res_prob = [0 for i in range(product(res_card))]
        # generate every assignment for the resulting factor
        for assignment_res in Factor._assignment_generator(res_card):
            assignment_f1 = assignment_by_mask(assignment_res, f1_mask)
            assignment_f2 = assignment_by_mask(assignment_res, f2_mask)
            pf1 = f1.prob[Factor._mapAssignmentToIndex(assignment_f1, f1.card)]
            pf2 = f2.prob[Factor._mapAssignmentToIndex(assignment_f2, f2.card)]
            res_prob[Factor._mapAssignmentToIndex(assignment_res, res_card)] = pf1 * pf2
        
        return Factor(res_uncond_vars, res_cond_vars, res_prob)
    
    def _mutliply_with_disjoint_variables(self, f2):
        f1 = self
        variables = f1.variables + f2.variables
        card = f1.card + f2.card
        prob = [0 for i in range(product(card))]
        i = 0
        for prob2 in f2.prob:
            for prob1 in f1.prob:
                prob[i] = prob1 * prob2
                i += 1
        return Factor(variables, card, prob)
            
    
    def reduce(self, evidences):
        pass
    
    def renormalize(self):
        pass
    
    @staticmethod
    def _mapAssignmentToIndex(assignment, card):
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
    def _mapIndexToAssignment(index, card):
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
            assignment = Factor._mapIndexToAssignment(i, self.card)
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
    return functools.reduce(operator.mul, iterable)


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
    