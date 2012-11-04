
import unittest
import factor


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
    
    def assertFloatVectorsEqual(self, vec1, vec2):
        self.assertEqual(len(vec1), len(vec2))
        for v1, v2 in zip(vec1, vec2):
            self.assertAlmostEqual(v1, v2, places=FactorBurglarTest.DOUBLE_PREC)
    
    def test_multiplication(self):
        factor_b_e = self.factor_burglary.multiply(self.factor_earthquake)
        correct_prob = [0.999*0.998, 0.001*0.998, 0.999*0.002, 0.001*0.002]
        self.assertFloatVectorsEqual(correct_prob, factor_b_e.prob)
    
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
        # compute factor for the whole network
        multiplication_order = [self.factor_burglary, self.factor_earthquake, self.factor_alarm, self.factor_marry_calls, self.factor_john_calls]
        f = multiplication_order[0]
        for f_next in multiplication_order[1:]:
            f = f_next.multiply(f)
        # marginalize over all variables except earthquake
        f_a = f.marginalize([self.var_burglary, self.var_alarm, self.var_marry_calls, self.var_john_calls])
        self.assertFloatVectorsEqual(self.factor_earthquake.prob, f_a.prob)
        # marginalize over all variables except burglary
        f_b = f.marginalize([self.var_earthquake, self.var_alarm, self.var_marry_calls, self.var_john_calls])
        self.assertFloatVectorsEqual(self.factor_burglary.prob, f_b.prob)
        

if __name__ == "__main__":
    unittest.main()
