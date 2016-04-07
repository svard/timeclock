(ns ^:figwheel-always net.svard.timeclock.component.table
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [net.svard.timeclock.date :as date]
            [cljsjs.react-bootstrap]))

(defonce target-seconds 27900)
(def table (js/React.createFactory js/ReactBootstrap.Table))

(defn row-context [context]
  (when context
    #js {:className context}))

(defn diff [seconds]
  (- seconds target-seconds))

(defn add-sign [x]
  (cond
    (<= x 0) (str x)
    (> x 0) (str "+" x)))

(defn edit [c {:keys [value]}]
  (om/update-state! c merge {:editing true :edit-text value}))

(defn change [c e]
  (om/update-state! c assoc :edit-text (.. e -target -value)))

(defn key-down [c e update-fn]
  (when (= (.-keyCode e) 13)
    (let [{:keys [edit-text]} (om/get-state c)]
      (om/update-state! c assoc :editing false)
      (update-fn edit-text))))

(defn label [c {:keys [value] :as props}]
  (dom/span #js {:className "view"
                 :onDoubleClick #(edit c props)} value))

(defn input [c update-fn]
  (dom/input #js {:className "form-control edit"
                  :value (:edit-text (om/get-state c))
                  :onChange #(change c %)
                  :onBlur #(om/set-state! c {:editing false})
                  :onKeyDown #(key-down c % update-fn)}))

(defui EditableCell
  Object 
  (render [this]
    (let [props (om/props this)
          {:keys [editing]} (om/get-state this)
          {:keys [update-fn]} (om/get-computed this)
          class (cond-> ""
                  editing (str "editing"))]
      (dom/td #js {:className (str "center-cell " class)}
        (label this props)
        (input this update-fn)))))

(def editable-cell (om/factory EditableCell))

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
          d (-> total (diff) (date/second->hour))]
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
        (dom/td #js {:className "center-cell"} (add-sign d))))))

(def row (om/factory Row))

(defui ReportTable
  Object
  (render [this]
    (let [rows (om/props this)
          sum-total (reduce #(+ %1 (:total %2)) 0 rows)
          d (-> (reduce #(+ %1 (diff (:total %2))) 0 rows) (date/second->hour))]
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
            (dom/td nil (add-sign d))))))))

(def report-table (om/factory ReportTable))
