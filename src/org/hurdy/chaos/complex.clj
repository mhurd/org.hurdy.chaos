(ns org.hurdy.chaos.complex)

;; Add the real parts together then the imaginary parts
(defn add
  "Complex addition"
  [c1 c2]
  (hash-map
    :r (+ (:r c1) (:r c2))
    :i (+ (:i c1) (:i c2)))
  )

;; To multiply to complex numbers such as (5+2i )(4+3i) ,
;; you can treat each one as a binomial and apply the FOIL method to find the product.
;; FOIL
;; multiply the firsts 5 × 4 = 20
;; multiply the outers 5 × 3i = 15i
;; multiply the inners 2i × 4 = 8i
;; multiply the lasts 2i × 3i = 6 i^2 = -6 (remember that i^2 = -1)
;; Then add:  20 + 15i + 8i -6 = 14 + 23i
(defn multiply
  "Complex Multipication"
  [c1 c2]
    (hash-map :r (+ (* (:r c1) (:r c2)) (- (* (:i c1) (:i c2)))) :i (+ (* (:r c1) (:i c2)) (* (:i c1) (:r c2))))
  )

;; The absolute value of a complex number is its distance from 0 on the
;; complex number plane.
(defn abs
  "Complex Absulute Value"
  [c]
  (Math/sqrt
   (+ (* (:r c) (:r c))
      (* (:i c) (:i c)))))