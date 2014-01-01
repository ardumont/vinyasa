# vinyasa

[Give your clojure workflow more flow](http://z.caudate.me/give-your-clojure-workflow-more-flow/)

The minimum leiningen version required for vinyasa is "2.3.4". Please do an upgrade of leiningen before using it.

    $ lein upgrade

## Installation

Add `vinyasa` to your `profile.clj`:

```clojure
{:user {:plugins [...]   
        :dependencies [....
                       [im.chit/vinyasa "0.1.0"]
                       ....]
        ....}
        :injections [...
                     (require '[vinyasa.inject :as inj])            
                     (inj/inject 'clojure.core
                       '[[vinyasa.inject inject]
                         [vinyasa.pull pull]
                         [vinyasa.lein lein]])
                     ...]
      }
```

## Quickstart:

If you are in emacs and are in a clojure project, you can run `nrepl-jack-in` and use the added functionality straight away once installed in `profiles.clj`.

```clojure
> (lein)
> (pull 'hiccup)
> (inject 'clojure.core '[[hiccup.core html]])
> (html [:p "Hello World"])
;;=> "<p>hello world</p>"
```

### pull

How many times have you forgotten a library dependency for `project.clj` and then had to restart your nrepl? `pull` is a convienient wrapper around the `pomegranate` library:

```clojure
> (require 'hiccup.core)
;; => java.io.FileNotFoundException: Could not locate hiccup/core__init.class or hiccup/core.clj on classpath:

> (require 'hiccup.core)
> (pull 'hiccup)
;; => {[org.clojure/clojure "1.2.1"] nil, 
;;     [hiccup "1.0.4"] #{[org.clojure/clojure "1.2.1"]}}

> (use 'hiccup.core)
> (html [:p "hello World"])
;; => "<p>hello World</p>"

> (pull 'hiccup "1.0.1")
;; => {[org.clojure/clojure "1.2.1"] nil, 
;;     [hiccup "1.0.1"] #{[org.clojure/clojure "1.2.1"]}}
```
### lein

Don't you wish that you had the power of leiningen within the repl itself? `lein` is that entry point. You don't have to open up another terminal window anymore, You can now run your commands in the repl!

```clojure
> (lein)
;; Leiningen is a tool for working with Clojure projects.
;;
;; Several tasks are available:
;; check               Check syntax and warn on reflection.
;; classpath           Write the classpath of the current project to output-file.
;; clean               Remove all files from paths in project's clean-targets.
;; cljsbuild           Compile ClojureScript source into a JavaScript file.
;;
;;  .....
;;  .....

> (lein javac)       ;; Compile java classes

> (lein install)     ;; Install to local maven repo

> (lein uberjar)     ;; Create a jar-file

> (lein push)        ;; Deploy on clojars I still use lein-clojars 
```
### inject

I find that when I am debugging, there are additional functionality that is needed which is not included in clojure.core. The most commonly used function is `pprint` and it is much better if the function came with me when I was debugging.

The best place to put all of these functions in in the `clojure.core` namespace
`inject` is used to add additional functionality to namespaces so that the functions are there right when I need them. Inject also works with macros and functions (unlike `intern` which only works with functions):

```clojure
> (inject 'clojure.core '[[clojure.repl dir]])
;; => will intern #'clojure.repl/dir to #'clojure.core/dir

> (clojure.core/dir clojure.core)
;; *
;; *'
;; *1
;; *2
;; *3
;; *agent*
;; *allow-unresolved-vars*
;; *assert*
;;
;; ...
;; ...
```    

`inject` can also work with multiple entries:

```clojure
> (inject 'clojure.core '[[clojure.repl doc source]])
;; => will create the var #'clojure.core/doc and #'clojure.core/source    
```

`inject` can also take a prefix:

```clojure
> (inject 'clojure.core '>> '[[clojure.repl doc source]])
;; => will create the var #'clojure.core/>>doc and #'clojure.core/>>source    
```

`inject` can use vector bindings to directly specify the name

```clojure
> (inject 'clojure.core '>> '[[clojure.repl doc [source source]]])
;; => will create the var #'clojure.core/>>doc and #'clojure.core/source    
```

### inject - installation

`inject` allows easy customisation of your clojure.core namespace by allowing injecting of the functions that you have always wanted to have in your `profiles.clj` file. Here is an example taken from my `profiles.clj`.

```clojure
{:user {:plugins [...]
        :dependencies [[spyscope "0.1.4"]
                       [org.clojure/tools.namespace "0.2.4"]
                       [io.aviso/pretty "0.1.8"]
                       [im.chit/vinyasa "0.1.0"]]
         :injections [(require 'spyscope.core)                
                      (require 'vinyasa.inject)            
                      (vinyasa.inject/inject 'clojure.core '>
                        '[[vinyasa.inject [inject inject]]
                          [vinyasa.pull [pull pull]]
                          [vinyasa.lein [lein lein]]
                          [clojure.tools.namespace.repl [refresh refresh]
                          [clojure.repl apropos dir doc find-doc source
                                        [root-cause >cause]]]
                          [io.aviso.repl [pretty-pst >pst]
                          [clojure.pprint pprint]])]}}
```
I have now imported the following vars into clojure.core and they will stay with me as I am coding in emacs:

  - from vinyasa:  
    - `inject` as `#'clojure.core/inject`
    - `pull` as `#'clojure.core/pull`
    - `lein` as `#'clojure.core/lein`
  - from tools.namespace:
    - `refresh` as `#'clojure.core/refresh`
  - from clojure.repl:   
    - `apropos` as `#'clojure.core/>apropos`
    - `dir` as `#'clojure.core/>dir`
    - `doc` as `#'clojure.core/>doc`
    - `find-doc` as `#'clojure.core/>find-doc`
    - `root-cause` as `#'clojure.core/>cause`
  - from io.aviso.repl:
    - `pretty-pst` as `#'clojure.core/>pst`   
  - from clojure.pprint:
    - `pprint` as `#'clojure.core/>pprint`

## License

Copyright © 2013 Chris Zheng

Distributed under the MIT License
