(ns ^:figwheel-always net.svard.timeclock.utils
  (:require [cognitect.transit :as t]
            [cljsjs.moment]
            [net.svard.timeclock.date :as date])
  (:import [goog.net XhrIo]))

(defn- transit-date-reader [v]
  (date/new-date (js/parseInt v)))

(def transit-date-writer
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
                                       {"m" transit-date-reader}}) (.getResponseText this)))))
      "POST" (t/write (t/writer :json {:handlers
                                       {net.svard.timeclock.date.Date transit-date-writer}}) remote)
      #js {"Content-Type" "application/transit+json"})))
