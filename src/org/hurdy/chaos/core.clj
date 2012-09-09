(ns org.hurdy.chaos.core
  (:gen-class))

(import org.lwjgl.opengl.Display)
(import org.lwjgl.opengl.DisplayMode)
(import org.lwjgl.opengl.GL11)

(defn startExampleGL []
  (do
    (. Display setDisplayMode (DisplayMode. 800 600))
    (. Display create)
    (. GL11 glMatrixMode GL11/GL_PROJECTION)
    (. GL11 glLoadIdentity)
    (. GL11 glOrtho 0 800 0 600 1 -1)
    (. GL11 glMatrixMode GL11/GL_MODELVIEW)
    (while (not (. Display isCloseRequested))
      (. GL11 glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
      (. GL11 glColor3f 0.5 0.5 1.0)
      (. GL11 glBegin GL11/GL_QUADS)
      (. GL11 glVertex2f 100 100)
      (. GL11 glVertex2f (+ 100 200) 100)
      (. GL11 glVertex2f (+ 100 200) (+ 100 200))
      (. GL11 glVertex2f 100 (+ 100 200))
      (. GL11 glEnd)
      (. Display update)
    )
    (. Display destroy)
  )
)

(defn -main[& args] (startExampleGL))