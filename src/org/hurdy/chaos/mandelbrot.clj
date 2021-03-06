(ns org.hurdy.chaos.mandelbrot
  (:use [org.hurdy.chaos.complex]))

(defn calc-path
  [x y max-iterations]
  (let  [c (hash-map :r x :i y)]
    (loop [z c
           iterations 0]
      (if (>= iterations max-iterations)
        (if (> (abs z) 2.0)
          {:point c :iterations iterations}
          {:point c :iterations iterations}
        )
        (if (> (abs z) 2.0)
          {:point c :iterations iterations}
          (recur (add c (multiply z z)) (inc iterations))
        )
      )
    )
  )
)