(ns ^:figwheel-always net.svard.timeclock.home
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [net.svard.timeclock.component.editable-cell :refer [editable-cell]]
            [net.svard.timeclock.date :as d]
            [net.svard.timeclock.utils :as utils]
            [cljsjs.react-bootstrap]))

(enable-console-print!)

(def pager (js/React.createFactory js/ReactBootstrap.Pager))
(def page-item (js/React.createFactory js/ReactBootstrap.PageItem))
(def table (js/React.createFactory js/ReactBootstrap.Table))

(defn- row-context [context]
  (when context
    #js {:className context}))

(defui Row
  static om/Ident
  (ident [this {:keys [_id]}]
    [:row/by_id _id])
  static om/IQuery
  (query [this]
    [:_id :arrival :leave :lunch :total])
  
  Object
  (update-arrival [this date new-time]
    (let [{:keys [leave lunch]} (om/props this)
          new-date (d/new-date (str date " " new-time))
          total (- (d/subtract leave new-date) lunch)]
      (om/transact! this `[(report/update {:arrival ~new-date :total ~total})])))

  (update-leave [this date new-time]
    (let [{:keys [arrival lunch]} (om/props this)
          new-date (d/new-date (str date " " new-time))
          total (- (d/subtract new-date arrival) lunch)]
      (om/transact! this `[(report/update {:leave ~new-date :total ~total})])))

  (update-lunch [this new-lunch]
    (let [{:keys [arrival leave]} (om/props this)
          l (d/hour->second new-lunch)
          total (- (d/subtract leave arrival) l)]
      (om/transact! this `[(report/update {:lunch ~l :total ~total})])))
  
  (render [this]
    (let [{:keys [arrival leave lunch total]} (om/props this)
          d (-> total (utils/diff) (d/second->hour))]
      (dom/tr (if (>= d 0)
                (row-context "success")
                (row-context "danger"))
        (dom/td #js {:className "center-cell"} (d/print-date arrival))
        (editable-cell (om/computed
                         {:value (d/print-time arrival)}
                         {:update-fn #(.update-arrival this (d/print-date arrival) %)}))
        (editable-cell (om/computed
                         {:value (d/print-time leave)}
                         {:update-fn #(.update-leave this (d/print-date leave) %)}))
        (editable-cell (om/computed
                         {:value (d/second->hour lunch)}
                         {:update-fn #(.update-lunch this %)}))
        (dom/td #js {:className "center-cell"} (d/second->hour total))
        (dom/td #js {:className "center-cell"} (utils/ceil (/ (d/second->hour total) 2)))
        (dom/td #js {:className "center-cell"} (utils/add-sign d))))))

(def row (om/factory Row))

(defui ReportTable
  Object
  (render [this]
    (let [rows (om/props this)
          sum-total (reduce #(+ %1 (:total %2)) 0 rows)
          d (-> (reduce #(+ %1 (utils/diff (:total %2))) 0 rows) (d/second->hour))]
      (table #js {:striped false :bordered false}
        (dom/thead nil
          (dom/tr nil
            (dom/th nil "Date")
            (dom/th nil "From")
            (dom/th nil "To")
            (dom/th nil "Lunch")
            (dom/th nil "Total")
            (dom/th nil "50%")
            (dom/th nil "Diff")))
        (dom/tbody nil
          (map row rows)
          (dom/tr nil
            (dom/td nil "")
            (dom/td nil "")
            (dom/td nil "")
            (dom/td nil "")
            (dom/td nil (d/second->hour sum-total))
            (dom/td nil "")
            (dom/td nil (utils/add-sign d))))))))

(def report-table (om/factory ReportTable))

(defui Home
  static om/IQueryParams
  (params [this]
    (let [now (d/new-date)]
      {:year (d/year now) :week (d/week now)}))
  
  static om/IQuery
  (query [this]
    `[({:home/reports ~(om/get-query Row)} {:year ~'?year :week ~'?week}) :home/date])
  
  Object
  (next-week [this]
    (om/transact! this '[(week/increment)]))

  (previous-week [this]
    (om/transact! this '[(week/decrement)]))
  
  (render [this]
    (let [{:keys [home/reports home/date]} (om/props this)]
      (when (not (nil? date))
        (dom/div nil
          (dom/h4 #js {:className "table-header"} (str "Week " (d/week date) " " (d/year date)))
          (report-table reports)
          (pager nil
            (page-item #js {:className "margin-small"
                            :onSelect #(.previous-week this)}
              (dom/i #js {:className "glyphicon glyphicon-chevron-left"}))
            (page-item #js {:className "margin-small"
                            :onSelect #(.next-week this)
                            :disabled (= date (d/new-date))}
              (dom/i #js {:className "glyphicon glyphicon-chevron-right"}))))))))
