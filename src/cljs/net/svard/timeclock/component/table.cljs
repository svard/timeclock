(ns ^:figwheel-always net.svard.timeclock.component.table
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [net.svard.timeclock.date :as date]
            [cljsjs.react-bootstrap]))

(defonce target-seconds 27900)
(def table (js/React.createFactory js/ReactBootstrap.Table))

(defn- row-context [context]
  (when context
    #js {:className context}))

(defn- second->hour [second]
  (let [hours (/ second 3600)]
    (-> (.round js/Math (* hours 100))
        (/ 100))))

(defn- diff [seconds]
  (- seconds target-seconds))

(defn- add-sign [x]
  (cond
    (<= x 0) (str x)
    (> x 0) (str "+" x)))

(defui EditableCell
  Object
  (render [this]
    (let [{:keys [value]} (om/props this)]
      (dom/td nil value))))

(def editable-cell (om/factory EditableCell))

(defui Row
  static om/Ident
  (ident [this {:keys [_id]}]
    [:row/by_id _id])
  static om/IQuery
  (query [this]
    '[:_id :arrival :leave :lunch :total])
  
  Object
  (render [this]
    (let [{:keys [arrival leave lunch total]} (om/props this)
          d (-> total (diff) (second->hour))]
      (dom/tr (if (>= d 0)
                (row-context "success")
                (row-context "danger"))
        (dom/td nil (date/print-date arrival))
        (editable-cell {:value (date/print-time arrival)})
        (editable-cell {:value (date/print-time leave)})
        (dom/td nil (second->hour lunch))
        (dom/td nil (second->hour total))
        (dom/td nil (add-sign d))))))

(def row (om/factory Row))

(defui ReportTable
  Object
  (render [this]
    (let [rows (om/props this)
          sum-total (reduce #(+ %1 (:total %2)) 0 rows)
          d (-> (reduce #(+ %1 (diff (:total %2))) 0 rows) (second->hour))]
      (table #js {:striped false :bordered false}
        (dom/thead nil
          (dom/tr nil
            (dom/th nil "Date")
            (dom/th nil "From")
            (dom/th nil "To")
            (dom/th nil "Lunch")
            (dom/th nil "Total")
            (dom/th nil "Diff")))
        (dom/tbody nil
          (map row rows)
          (dom/tr nil
            (dom/td nil "")
            (dom/td nil "")
            (dom/td nil "")
            (dom/td nil "")
            (dom/td nil (second->hour sum-total))
            (dom/td nil (add-sign d))))))))

(def report-table (om/factory ReportTable))
