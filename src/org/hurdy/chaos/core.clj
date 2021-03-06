(ns org.hurdy.chaos.core
  (:use [org.hurdy.chaos.mandelbrot])
  (:gen-class ))

(import org.lwjgl.opengl.Display)
(import org.lwjgl.opengl.DisplayMode)
(import org.lwjgl.opengl.GL11)
(import org.lwjgl.opengl.GL12)
(import org.lwjgl.opengl.GLContext)
(import org.lwjgl.input.Mouse)
(import org.lwjgl.input.Keyboard)
(import org.lwjgl.Sys)
(import java.awt.image.BufferedImage)
(import java.nio.ByteBuffer)
(import java.awt.Color)

(def last-frame (ref 0))
(def last-fps (ref 0))
(def fps (ref 0))
(def rotation (ref 0))
(def x (ref 400))
(def y (ref 300))

(defn colour []
  ;; set the color of the quad (R,G,B,A)
  (. GL11 glColor3f 1.0 1.0 1.0)
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

(def calc-model (ref nil))
(def buffer (ref nil))

(def current-complex-plane-width (ref 3.0))
(def current-complex-plane-height (ref 3.0))
(def current-offset-x (ref (- (* @current-complex-plane-width 0.73)))) ;; *0.73
(def current-offset-y (ref (- (* @current-complex-plane-height 0.5)))) ;; *0.5

(def draw-in-progress (ref false))

(defn load-texture [^BufferedImage buffered-image]
  (let [width (.getWidth buffered-image)
        height (.getHeight buffered-image)
        pixels (int-array (* width height))
        bytes-per-pixel 4
        x-range (range 0 width 1)
        y-range (range 0 height 1)
        texture-id (. GL11 glGenTextures)
        raster (.getRaster buffered-image)]
    (dosync
      (ref-set buffer (. ByteBuffer allocateDirect (* width height bytes-per-pixel)))
      )
    (.clear @buffer)
    (.put @buffer (bytes (.getDataElements raster 0 0 width height nil)))
    (.rewind @buffer)
    (. GL11 glBindTexture GL11/GL_TEXTURE_2D texture-id)
    ;; Setup wrap mode
    (. GL11 glTexParameteri GL11/GL_TEXTURE_2D GL11/GL_TEXTURE_WRAP_S GL12/GL_CLAMP_TO_EDGE)
    (. GL11 glTexParameteri GL11/GL_TEXTURE_2D GL11/GL_TEXTURE_WRAP_T GL12/GL_CLAMP_TO_EDGE)
    ;; Setup texture scaling filtering
    (. GL11 glTexParameteri GL11/GL_TEXTURE_2D GL11/GL_TEXTURE_MIN_FILTER GL11/GL_LINEAR)
    (. GL11 glTexParameteri GL11/GL_TEXTURE_2D GL11/GL_TEXTURE_MAG_FILTER GL11/GL_LINEAR)
    ;; Send texel data to OpenGL
    (. GL11 glTexImage2D GL11/GL_TEXTURE_2D 0 GL11/GL_RGBA8 width height 0 GL11/GL_RGBA GL11/GL_UNSIGNED_BYTE @buffer)
    texture-id
    )
  )

(defn update-buffer [^BufferedImage buffered-image texture-id]
  (let [width (.getWidth buffered-image)
        height (.getHeight buffered-image)
        raster (.getRaster buffered-image)]
    (.clear @buffer)
    (.put @buffer (bytes (.getDataElements raster 0 0 width height nil)))
    (.rewind @buffer)
    ;; Send texel data to OpenGL
    (. GL11 glTexImage2D GL11/GL_TEXTURE_2D 0 GL11/GL_RGBA8 width height 0 GL11/GL_RGBA GL11/GL_UNSIGNED_BYTE @buffer)
    )
  )

(defn calc-pixel-color [iterations max-iterations]
  (if (or (< iterations 10)
        (= iterations max-iterations))
    (Color. 0 0 0)
    (let [gray (int (/ (* iterations 255) max-iterations))
          r gray
          g (min (int (/ (* 5 (* gray gray)) 255)) 255)
          b (min (int (+ 40 (/ (* 5 (* gray gray)) 255))) 255)]
      (Color. r g b)))
  )

(defn doMandelbrot [agent surface-width surface-height texture-id]
  (dosync
    (ref-set draw-in-progress true)
    )
  (println (str "current-offset: " @current-offset-x "," @current-offset-y))
  (println (str "current-complex-plane: " @current-complex-plane-width "," @current-complex-plane-height))
  (doseq [i (range 0 surface-width)
          j (range 0 surface-height)]
    (let [max-iterations 50
          p (+ @current-offset-x (* @current-complex-plane-width (/ i surface-width)))
          q (+ @current-offset-y (* @current-complex-plane-height (/ j surface-height)))
          result (calc-path p q max-iterations)
          itr (:iterations result)]

      (.setRGB @calc-model i j (.getRGB (calc-pixel-color itr max-iterations)))
      )
    )
  (dosync
    (ref-set draw-in-progress false)
    )
  )

(defn pollInput [calculator mandelbrot-fn]
  (if (and (Mouse/next) (Mouse/getEventButtonState) (not @draw-in-progress))
    (let [mouse-x (Mouse/getEventX)
          mouse-y (Mouse/getEventY)]
    (if (= (Mouse/getEventButton) 0)
      (do
        (dosync
          (println (str "Left button pressed " mouse-x "," mouse-y))
          (ref-set current-complex-plane-width (int (/ @current-complex-plane-width 2)))
          (ref-set current-complex-plane-height (int (/ @current-complex-plane-height 2)))
          (ref-set current-offset-x (- (* @current-complex-plane-width 0.73)))
          (ref-set current-offset-y (- (* @current-complex-plane-height 0.5)))
          )
        (send calculator mandelbrot-fn)
        )
      (if (= (Mouse/getEventButton) 1)
        (do
          (dosync
            (println (str "Right button pressed " mouse-x "," mouse-y))
            (ref-set current-complex-plane-width (int (* @current-complex-plane-width 2)))
            (ref-set current-complex-plane-height (int (* @current-complex-plane-height 2)))
            (ref-set current-offset-x (- (* @current-complex-plane-width 0.73)))
            (ref-set current-offset-y (- (* @current-complex-plane-height 0.5)))
            )
          (send calculator mandelbrot-fn)
          )
        )
      )
    )
  )
  )

(defn render-gl [texture-id calculator width height]
  (update-fps)
  ;; still need to work out how to replace the texture/buffer in the render loop without killing
  ;; my machine :-)
  (update-buffer @calc-model texture-id)
  (. GL11 glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
  (. GL11 glEnable GL11/GL_BLEND)
  (. GL11 glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)
  (. GL11 glPushMatrix)
  (. GL11 glBindTexture GL11/GL_TEXTURE_2D texture-id)
  (. GL11 glBegin GL11/GL_QUADS)
  (. GL11 glTexCoord2f 0 0)
  (. GL11 glVertex2f 0 0)
  (. GL11 glTexCoord2f 1 0)
  (. GL11 glVertex2f width 0)
  (. GL11 glTexCoord2f 1 1)
  (. GL11 glVertex2f width height)
  (. GL11 glTexCoord2f 0 1)
  (. GL11 glVertex2f 0 height)
  (. GL11 glEnd)
  (. GL11 glPopMatrix)
  )

(defn startExampleGL []
  (do
    (let [width 400
          height 400
          start-offset-x -2.2
          start-offset-y -1.6
          start-complex-plane-width 3.0
          start-complex-plane-height 3.1
          calculator (agent [])]
      ;; Create the display
      (. Display setDisplayMode (DisplayMode. width height))
      (. Display create)
      ;; GL Stuff
      (. GL11 glMatrixMode GL11/GL_PROJECTION)
      (. GL11 glLoadIdentity)
      (. GL11 glOrtho 0 width height 0 -1 1)
      (. GL11 glMatrixMode GL11/GL_MODELVIEW)
      (. GL11 glClearColor 0 1 0 0)
      (get-delta)
      (dosync
        (ref-set last-fps (get-time))
        )
      ;; set up a blank buffered image
      (dosync
        (ref-set calc-model (BufferedImage. width height BufferedImage/TYPE_4BYTE_ABGR))
        )
      (let [graphics (.createGraphics @calc-model)]
        (.setColor graphics (Color. 1.0 1.0 1.0 0.5))
        (.fillRect graphics 0 0 width height)
        (.setColor graphics Color/red)
        (.drawString graphics "foobar" 10 64)
        )
      (. GL11 glEnable GL11/GL_TEXTURE_2D)
      ;; start the agent calculator working
      (let [texture-id (load-texture @calc-model)
            mandelbrot-fn #(doMandelbrot % width height texture-id)
            ]
        (send calculator mandelbrot-fn)
        (while (not (. Display isCloseRequested))
          (render-gl texture-id calculator width height)
          (pollInput calculator mandelbrot-fn)
          (. Display update)
          (. Display sync 120)
          )
        (. GL11 glBindTexture GL11/GL_TEXTURE_2D 0)
        (. Display destroy)
        )
      )
    )
  )

(defn -main [& args] (startExampleGL))