
import unittest
import functools
import factor

# docs: http://docs.python.org/3/library/unittest.html#assert-methods


class FactorBurglarTest(unittest.TestCase):
    DOUBLE_PREC = 4
    
    def setUp(self):
        self.var_burglary = factor.Variable("Burglary", ["b0", "b1"])
        self.var_earthquake = factor.Variable("Earthquake", ["e0", "e1"])
        self.var_alarm = factor.Variable("Alarm", ["a0", "a1"])
        self.var_john_calls = factor.Variable("JohnCalls", ["j0", "j1"])
        self.var_marry_calls = factor.Variable("MarryCalls", ["m0", "m1"])
        self.factor_burglary = factor.Factor([self.var_burglary], [0.999, 0.001])
        self.factor_earthquake = factor.Factor([self.var_earthquake], [0.998, 0.002])
        self.factor_alarm = factor.Factor([self.var_alarm, self.var_burglary, self.var_earthquake], [0.999, 0.001, 0.06, 0.94, 0.71, 0.29, 0.05, 0.95])
        self.factor_john_calls = factor.Factor([self.var_john_calls, self.var_alarm], [0.95, 0.05, 0.10, 0.90])
        self.factor_marry_calls = factor.Factor([self.var_marry_calls, self.var_alarm], [0.99, 0.01, 0.3, 0.7])
        self.all_factors = [self.factor_burglary, self.factor_earthquake, self.factor_alarm, self.factor_john_calls, self.factor_marry_calls]
    
    def assertFloatVectorsEqual(self, vec1, vec2):
        self.assertEqual(len(vec1), len(vec2))
        for v1, v2 in zip(vec1, vec2):
            self.assertAlmostEqual(v1, v2, places=FactorBurglarTest.DOUBLE_PREC)
    
    def test_invalid_scope(self):
        scope = []
        prob = [0, 1]
        self.assertRaises(factor.FactorException, factor.Factor, scope, prob)
    
    def test_invalid_prob_length(self):
        scope = [factor.Variable("X", ["x0", "x1"])]
        prob = [0]
        self.assertRaises(factor.FactorException, factor.Factor, scope, prob)
    
    def test_invalid_prob_value(self):
        scope = [factor.Variable("X", ["x0", "x1"])]
        prob = [0, -0.5]
        self.assertRaises(factor.FactorException, factor.Factor, scope, prob)
    
    def test_value_correct(self):
        assignment_dict = {self.var_burglary: 0, self.var_earthquake: 0, self.var_alarm: 0, self.var_john_calls: 1}
        f_value = self.factor_alarm.value(assignment_dict)
        self.assertEqual(f_value, self.factor_alarm.prob[0])
    
    def test_value_incomplete(self):
        assignment_dict = {self.var_burglary: 0, self.var_alarm: 0}
        self.assertRaises(factor.FactorException, self.factor_alarm.value, assignment_dict)
    
    def test_multiplication(self):
        factor_b_e = self.factor_burglary.multiply(self.factor_earthquake)
        correct_prob = [0.999*0.998, 0.001*0.998, 0.999*0.002, 0.001*0.002]
        self.assertFloatVectorsEqual(correct_prob, factor_b_e.prob)
    
    def test_reduce(self):
        evidence = {self.var_burglary: 1}
        f_b_reduced = self.factor_burglary.reduce(evidence)
        correct_prob_reduced = list(self.factor_burglary.prob)
        correct_prob_reduced[0] = 0
        self.assertFloatVectorsEqual(f_b_reduced.prob, correct_prob_reduced)
    
    def test_p_alarm_on(self):
        # computation of P(A = true) by hand
        p_BEA = 0.001 * 0.002 * 0.95
        p_bEA = 0.999 * 0.002 * 0.29
        p_BeA = 0.001 * 0.998 * 0.94
        p_beA = 0.999 * 0.998 * 0.001
        p_A = p_BEA + p_bEA + p_BeA + p_beA
        f_BE = self.factor_earthquake.multiply(self.factor_burglary)
        f_BEA = self.factor_alarm.multiply(f_BE)
        f_A = f_BEA.marginalize([self.var_burglary, self.var_earthquake])
        self.assertAlmostEqual(p_A, f_A.prob[1], places=FactorBurglarTest.DOUBLE_PREC)
    
    def test_multiplication_and_marginalization(self):
        factor_b_e = self.factor_burglary.multiply(self.factor_earthquake)
        factor_b = factor_b_e.marginalize([self.var_earthquake])
        self.assertFloatVectorsEqual(self.factor_burglary.prob, factor_b.prob)
    
    def test_multiplication_and_marginalization_whole_network(self):
        # compute factor for the whole network and then sum everything out except the "root" variables
        f = functools.reduce(lambda x,y: x.multiply(y), self.all_factors)
        # marginalize over all variables except earthquake
        f_a = f.marginalize([self.var_burglary, self.var_alarm, self.var_marry_calls, self.var_john_calls])
        self.assertFloatVectorsEqual(self.factor_earthquake.prob, f_a.prob)
        # marginalize over all variables except burglary
        f_b = f.marginalize([self.var_earthquake, self.var_alarm, self.var_marry_calls, self.var_john_calls])
        self.assertFloatVectorsEqual(self.factor_burglary.prob, f_b.prob)
        
    def test_complex_evidence_and_inference(self):
        # P(B|j,m) ~ [0.716, 0.284]
        evidence = {self.var_john_calls: 1, self.var_marry_calls: 1}
        reduced_factors = [f.reduce(evidence) for f in self.all_factors]
        f_all = functools.reduce(lambda x,y: x.multiply(y), reduced_factors)
        # marginalize over all variables except burglary
        f_marginalized = f_all.marginalize([self.var_earthquake, self.var_alarm, self.var_marry_calls, self.var_john_calls])
        f_result = f_marginalized.renormalize()
        self.assertFloatVectorsEqual([0.71583, 0.28417], f_result.prob)
        

if __name__ == "__main__":
    unittest.main()
