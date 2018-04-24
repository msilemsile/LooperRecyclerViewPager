# LoopRecyclerViewPager
LooperRecyclerViewPager  replace  LooperViewPager

in case, the RecyclerView has 4 children!!!


1 --> 2 --> 3 --> 4|startEdge|1 --> 2 --> 3 --> 4|initPosition|1 --> 2 --> 3 --> 4|endEdge|1 --> 2 --> 3 --> 4


When current scroll position = startEdge|| endEdge ,Then current scroll position will reset = initPosition

orientation = HORIZONTAL

![Image](https://github.com/msilemsile/LooperRecyclerViewPager/blob/master/demo.gif)

orientation = VERTICAL

![Image](https://github.com/msilemsile/LooperRecyclerViewPager/blob/master/demo2.gif)
