(ns ^:figwheel-always net.svard.timeclock.utils
  (:require [cognitect.transit :as t]
            [om.next :as om]
            [cljsjs.moment]
            [net.svard.timeclock.date :as date])
  (:import [goog.net XhrIo]))

(defonce target-seconds 27900)

(defn diff [seconds]
  (- seconds target-seconds))

(defn add-sign [x]
  (cond
    (<= x 0) (str x)
    (> x 0) (str "+" x)))

(defn- transit-date-reader [v]
  (date/new-date (js/parseInt v)))

(def ^:private transit-date-writer
  (t/write-handler
    (constantly "m")
    (fn [v] (.valueOf (.-moment v)))
    (fn [v] (.valueOf (.-moment v)))))

(defn transit-post [url]
  (fn [{:keys [remote]} cb] 
    (.send XhrIo url
      (fn [e]
        (this-as this
          (cb (t/read (t/reader :json {:handlers
                                       {"m" transit-date-reader}}) (.getResponseText this)) remote)))
      "POST" (t/write (t/writer :json {:handlers
                                       {net.svard.timeclock.date.Date transit-date-writer}}) remote)
      #js {"Content-Type" "application/transit+json"})))
