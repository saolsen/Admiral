(ns admiral.render.core)

;; Want the "gameworld" to be 1000 x 500 so I need to scale that size
;; to the size of the canvas for drawing.

(defn scale-canvas
  [ctx]
  (let [width (.-width (.-canvas ctx))
        scale (/ width 1000)]
    (.scale ctx scale scale)))

;; Util
(defn get-canvas-element
  [id]
  (.getElementById js/document id))

(defn reset-window-size!
  [canvas]
  (let [width (.-innerWidth js/window)
        height (.-innerHeight js/window)]
    (aset canvas "width" width)
    (if (< height (/ width 2))
      (aset canvas "height" height)
      (aset canvas "height" (/ width 2)))))

(defn to-color [& rgbas]
  (let [csv (apply str (interpose ", " rgbas))]
    (str "rgb(" csv ")")))

;; Drawing Primitives
(def twopi (* 2 (.-PI js/Math)))

(defn draw-rectangle
  [ctx color pos w h]
  (let [[x y] pos]
    (aset ctx "fillStyle" (apply to-color color))
    (.fillRect ctx x y w h)))

(defn draw-circle
  [ctx color pos r]
  (let [[x y] pos]
  	(aset ctx "fillStyle" (apply to-color color))
  	(.beginPath ctx)
  	(.arc ctx x y r 0 twopi)
  	(.closePath ctx)
  	(.fill ctx)))

(defn draw-line [ctx color width points]
  (let [[startx starty] (first points)]
    (.beginPath ctx)
    (.moveTo ctx startx starty)
    (doseq [[x y] (rest points)]
      (.lineTo ctx x y))
    (.closePath ctx)
    (aset ctx "lineWidth" width)
    (.stroke ctx)))

(defn draw-ship
  [ctx faction]
  (let [color (condp = faction
                :red (to-color 200 0 0)
                :blue (to-color 0 0 200))]
    (aset ctx "fillStyle" color)
    (.beginPath ctx)
    (.moveTo ctx 0 10)
    (.lineTo ctx 10 -10)
    (.lineTo ctx -10 -10)
    (.lineTo ctx 0 10)
    (.fill ctx)))

;; Drawing Entities
(defmulti draw-entity :model)

(defmethod draw-entity :ship [{:keys [faction rotation pos]} ctx]
  (let [[x y] pos]
    (.save ctx)
    (.translate ctx x y)
    (.rotate ctx rotation)
    (draw-ship ctx faction)
    (.restore ctx)))

;; Renderer
(defprotocol Renderer
  "A context used to render admiral to the screen."
  (render [this world] "Renders the world to the screen."))

(deftype Canvas [context]
  Renderer
  (render [_ gamestate]
    (.save context)
    (scale-canvas context)
    (doseq [[id entity] (:entities gamestate)]
      (draw-entity entity context))
    (.restore context)))

(defn create-canvas-renderer
  "Creates a renderer from a canvas id."
  [canvas-id]
  (let [canvas (get-canvas-element canvas-id)
        context (.getContext canvas "2d")]

    ;; Resize canvas when screen resizes
    ;; Not sure if this is what I want when I have UI.
    (aset js/window "onresize"
          (fn [e] (reset-window-size! canvas)))
    (reset-window-size! canvas)
    
    (Canvas. context)))
