(ns net.svard.timeclock.parser
  (:require [om.next.server :as om]
            [clojure.tools.logging :as log]
            [net.svard.timeclock.report :as report]))

(defmulti readf om/dispatch)

(defmethod readf :timeclock/reports
  [{:keys [db query]} k {:keys [year week] :as params}]
  (log/info "Parsing" k query params)
  {:value (vec (report/get-by-week db year week))})
