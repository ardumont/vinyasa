(ns vinyasa.stacktrace
  (:require clojure.stacktrace
            io.aviso.exception))

(alter-var-root #'clojure.stacktrace/print-cause-trace
                (constantly io.aviso.exception/write-exception))
