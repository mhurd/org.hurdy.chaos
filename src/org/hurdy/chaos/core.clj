(ns org.hurdy.chaos.core
  (:gen-class))

(import java.io.File)

(defn init []
  (do
    (println (. System getProperty "java.library.path"))
    (println (. (File. ".") getAbsolutePath))
    (import org.lwjgl.Sys)
    )
  )

(defn -main[& args] (init))


