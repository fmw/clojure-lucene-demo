Clojure Lucene Demo
===================

The reason I wrote this code is to demonstrate a problem I'm having with
filters in Lucene. As soon as I apply a filter my searcher returns zero
results. I've [documented this in a test][test-search]. You can also navigate
to the [search function][search] directly. Do you have any idea what I'm doing
wrong? I was hoping that this abstract test implementation would isolate the
problem, but it didn't.

Now I took the time to write this abstract example of how I use Lucene with
Clojure, I'm planning to keep it up as reference for others. That way, the
code will serve some purpose once the problem is solved. Of course, any errors
or non-idiomatic usage should be fixed by that point.

Please feel free to contact me using the email address below in case
you have any questions or suggestions.

[test-search]: https://github.com/fmw/clojure-lucene-demo/blob/master/test/clojure_lucene_demo/test/core.clj#L302

[search]: https://github.com/fmw/clojure-lucene-demo/blob/master/src/clojure_lucene_demo/core.clj#L115

Copyright 2011, F.M. (Filip) de Waard <<fmw@vix.io>>.
Distributed under the Apache License, version 2 (see the LICENSE file).
