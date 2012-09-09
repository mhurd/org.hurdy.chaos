(ns org.hurdy.chaos.core
  (:gen-class))

(import org.lwjgl.opengl.Display)
(import org.lwjgl.opengl.DisplayMode)
(import org.lwjgl.opengl.GL11)
(import org.lwjgl.input.Mouse)
(import org.lwjgl.input.Keyboard)
(import org.lwjgl.Sys)

(def last-frame (ref 0))
(def last-fps (ref 0))
(def fps (ref 0))
(def rotation (ref 0))
(def x (ref 400))
(def y (ref 300))

(defn colour []
  ;; set the color of the quad (R,G,B,A)
  (. GL11 glColor3f 0.5 0.5 1.0)
)

(defn get-time
  "Get the accurate system time (in millseconds)"
  []
  (/ (* (. Sys getTime) 1000) (. Sys getTimerResolution))
  )

(defn get-delta
  "Calculate how many milliseconds have passed since last frame"
  []
  (let [time (get-time)
        delta (- time @last-frame)]
    (dosync
      (ref-set last-frame time)
    )
    delta
  )
)

(defn update-fps
  "Calculate the FPS and set it in the title bar"
  []
  (do
    (if (> (- (get-time) @last-fps) 1000)
      (do
        (. Display setTitle (str "FPS: " @fps))
        (dosync
          (ref-set fps 0)
          (ref-set last-fps (+ @last-fps 1000))
        )
      )
    )
    (dosync
      (ref-set fps (inc @fps))
    )
  )
)

(defn render-gl []
  (do
    (dosync
      (ref-set rotation (+ @rotation (* 0.15 (get-delta))))
    )
    (update-fps)
    (. GL11 glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
    (colour)
    (. GL11 glPushMatrix);
    (. GL11 glTranslatef @x  @y  0)
    (. GL11 glRotatef @rotation 0 0 1)
    (. GL11 glTranslatef (- @x)  (- @y)  0)
    (. GL11 glBegin GL11/GL_QUADS)
    (. GL11 glVertex2f (- @x 140) (- @y 140))
    (. GL11 glVertex2f (+ @x 140) (- @y 140))
    (. GL11 glVertex2f (+ @x 140) (+ @y 140))
    (. GL11 glVertex2f (- @x 140) (+ @y 140))
    (. GL11 glEnd)
    (. GL11 glPopMatrix)
  )
)

(defn startExampleGL []
  (do
    ;; Create the display
    (. Display setDisplayMode (DisplayMode. 1024 768))
    (. Display create)
    ;; GL Stuff
    (. GL11 glMatrixMode GL11/GL_PROJECTION)
    (. GL11 glLoadIdentity)
    (. GL11 glOrtho 0 1024 0 768 1 -1)
    (. GL11 glMatrixMode GL11/GL_MODELVIEW)
    (get-delta)
    (dosync
      (ref-set last-fps (get-time))
    )
    (while (not (. Display isCloseRequested))
      (render-gl)
      (. Display update)
      (. Display sync 60)
    )
    (. Display destroy)
  )
)

(defn -main[& args] (startExampleGL))