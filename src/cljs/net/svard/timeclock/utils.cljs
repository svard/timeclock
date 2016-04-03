(ns ^:figwheel-always net.svard.timeclock.utils
  (:require [cognitect.transit :as t]
            [cljsjs.moment])
  (:import [goog.net XhrIo]))

(defn- transit-date-handler [v]
  (js/moment (js/parseInt v)))

(defn transit-post [url]
  (fn [{:keys [remote]} cb]
    (.send XhrIo url
      (fn [e]
        (this-as this
          (cb (t/read (t/reader :json {:handlers
                                       {"m" transit-date-handler}}) (.getResponseText this)))))
      "POST" (t/write (t/writer :json) remote)
      #js {"Content-Type" "application/transit+json"})))
