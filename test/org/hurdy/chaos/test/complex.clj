(ns org.hurdy.chaos.test.complex
  (:use [org.hurdy.chaos.complex])
  (:use [clojure.test]))

;; Test that (1 + 2i) + (3 - i) = (4 + i)
(deftest test-add []
  (is
      (= (add {:r 1 :i 2} {:r 3 :i -1}) {:r 4 :i 1})
  )
)

;; Test that (5 + 2i) * (4 + 3i) = (14 + 23i)
(deftest test-multiply []
  (is
      (= (multiply {:r 5 :i 2} {:r 4 :i 3}) {:r 14 :i 23})
  )
)