(ns me.yiwan.puck.main
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.init :refer [init]]
            [me.yiwan.puck.check :refer [check]]
            [mount.core :as mount]))

(defn usage [options-summary]
  (->> ["Puck, a simple markdown bloging application"
        ""
        "Usage: puck [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  start  Start application"
        "  init   Initialize"
        "  check  Check files, directories"
        ""
        (string/join \newline)]))

(def cli-options [["-w" "--working-directory path" "Wroking directory"
                   :default (.getAbsolutePath fs/*cwd*)
                   :parse-fn #(.getAbsolutePath (io/file %))
                   :validate [#(fs/directory? %) "not a directory"]]
                  ["-h" "--help"]])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options)
      {:exit-message (usage summary) :ok? true}
      errors
      {:exit-message (error-msg errors)}
      (and (= 1 (count arguments))
           (#{"start" "init" "check"} (first arguments)))
      {:action (first arguments) :options options}
      :else
      {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (case action
        "start"
        (->
         (mount/except [#'me.yiwan.puck.init/init])
         (mount/with-args options)
         mount/start)
        "init"
        (->
         (mount/only [#'me.yiwan.puck.init/init])
         (mount/with-args options)
         mount/start)
        "check"
        (->
         (mount/only [#'me.yiwan.puck.conf/conf
                      #'me.yiwan.puck.check/check])
         (mount/with-args options)
         mount/start)))))
