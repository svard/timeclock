(ns ^:figwheel-always net.svard.timeclock.component.table
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [net.svard.timeclock.date :as date]
            [net.svard.timeclock.utils :as utils]
            [net.svard.timeclock.component.row :refer [row]]
            [cljsjs.react-bootstrap]))

(def table (js/React.createFactory js/ReactBootstrap.Table))

(defui ReportTable
  Object
  (render [this]
    (let [rows (om/props this)
          sum-total (reduce #(+ %1 (:total %2)) 0 rows)
          d (-> (reduce #(+ %1 (utils/diff (:total %2))) 0 rows) (date/second->hour))]
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
            (dom/td nil (date/second->hour sum-total))
            (dom/td nil "")
            (dom/td nil (utils/add-sign d))))))))

(def report-table (om/factory ReportTable))
