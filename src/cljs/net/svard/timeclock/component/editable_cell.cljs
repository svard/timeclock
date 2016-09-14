(ns ^:figwheel-always net.svard.timeclock.component.editable-cell
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

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
  (dom/input #js {:ref "editField"
                  :className "form-control edit"
                  :value (or (:edit-text (om/get-state c)) "")
                  :onChange #(change c %)
                  :onBlur #(om/set-state! c {:editing false})
                  :onKeyDown #(key-down c % update-fn)}))

(defui EditableCell
  Object
  (initLocalState [this]
    {:editing false})

  (componentDidUpdate [this prev-props prev-state]
    (when (:editing (om/get-state this))
      (let [node (dom/node this "editField")]
        (.focus node))))
  
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
