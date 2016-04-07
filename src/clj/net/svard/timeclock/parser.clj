(ns net.svard.timeclock.parser
  (:require [om.next.server :as om]
            [clojure.tools.logging :as log]
            [net.svard.timeclock.report :as report]))

(defmulti readf om/dispatch)

(defmethod readf :timeclock/reports
  [{:keys [db query]} k {:keys [year week] :as params}]
  (log/info "Parsing" k query params)
  {:value (vec (report/get-by-week db year week))})

(defmulti mutatef om/dispatch)

(defmethod mutatef 'report/update
  [{:keys [db query]} k params] 
  (log/info "Transact" params) 
  {:value {:keys [:total]}
   :action (fn []
             (report/update db params))})
