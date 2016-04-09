(ns ^:figwheel-always net.svard.timeclock.component.row
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [net.svard.timeclock.date :as date]
            [net.svard.timeclock.utils :as utils]
            [net.svard.timeclock.component.editable-cell :refer [editable-cell]]))

(defn row-context [context]
  (when context
    #js {:className context}))

(defui Row
  static om/Ident
  (ident [this {:keys [_id]}]
    [:row/by_id _id])
  static om/IQuery
  (query [this]
    '[:_id :arrival :leave :lunch :total])
  
  Object
  (update-arrival [this date new-time]
    (let [{:keys [leave lunch]} (om/props this)
          new-date (date/new-date (str date " " new-time))
          total (- (date/subtract leave new-date) lunch)]
      (om/transact! this `[(report/update {:arrival ~new-date :total ~total})])))

  (update-leave [this date new-time]
    (let [{:keys [arrival lunch]} (om/props this)
          new-date (date/new-date (str date " " new-time))
          total (- (date/subtract new-date arrival) lunch)]
      (om/transact! this `[(report/update {:leave ~new-date :total ~total})])))

  (update-lunch [this new-lunch]
    (let [{:keys [arrival leave]} (om/props this)
          l (date/hour->second new-lunch)
          total (- (date/subtract leave arrival) l)]
      (om/transact! this `[(report/update {:lunch ~l :total ~total})])))
  
  (render [this]
    (let [{:keys [arrival leave lunch total]} (om/props this)
          d (-> total (utils/diff) (date/second->hour))]
      (dom/tr (if (>= d 0)
                (row-context "success")
                (row-context "danger"))
        (dom/td #js {:className "center-cell"} (date/print-date arrival))
        (editable-cell (om/computed
                         {:value (date/print-time arrival)}
                         {:update-fn #(.update-arrival this (date/print-date arrival) %)}))
        (editable-cell (om/computed
                         {:value (date/print-time leave)}
                         {:update-fn #(.update-leave this (date/print-date leave) %)}))
        (editable-cell (om/computed
                         {:value (date/second->hour lunch)}
                         {:update-fn #(.update-lunch this %)}))
        (dom/td #js {:className "center-cell"} (date/second->hour total))
        (dom/td #js {:className "center-cell"} (/ (date/second->hour total) 2))
        (dom/td #js {:className "center-cell"} (utils/add-sign d))))))

(def row (om/factory Row))
