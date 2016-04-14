(ns ^:figwheel-always net.svard.timeclock.component.stats-row
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [net.svard.timeclock.date :as date]))

(defn date-cell [{:keys [date time]}]
  (let [hours (date/second->hour time)
        date-str (.format (.-moment date) "MMM Do")]
    (dom/td nil (str date-str " " hours "h"))))

(defui Row
  static om/Ident
  (ident [this {:keys [_id]}]
    [:row/by-id _id])
  
  static om/IQuery
  (query [this]
    [:_id :longest :shortest :avg :sum])

  Object
  (render [this]
    (let [{:keys [_id longest shortest avg sum]} (om/props this)]
      (println _id)
      (dom/tr nil
        (dom/td nil _id)
        (date-cell longest)
        (date-cell shortest)
        (dom/td nil (date/second->hour avg))
        (dom/td nil (date/second->hour sum))))))

(def row (om/factory Row))
