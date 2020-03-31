# libertymon

Liberty WAR file that periodically writes statistics about the Liberty server to a CSV file (e.g. `logs/libertymon.csv`).

There are [two versions of the WAR](https://github.com/kgibm/libertymon/releases) depending on which features you want to enable in Liberty:

* [liberty-ejb.war](https://github.com/kgibm/libertymon/tree/master/libertymon-ejb): Requires Liberty features ejbLite-3.2, jsp-2.3, and monitor-1.0.
* [liberty-servlet.war](https://github.com/kgibm/libertymon/tree/master/libertymon-servlet): Requires Liberty features jsp-2.3 and monitor-1.0; however, in addition, it also requires `<webContainer deferServletLoad="false" />`.

## Example Output

```
Time,Name,PID,Classes,JavaHeap,JVMHeap,TotalThreads,CPUThreads,SystemLoadAverage1Min,ProcessCPUCumulative,ProcessCPUDiff,ProcessCPU%,GCsCumulative,GCsDiff,GCTimeCumulative,GCTimeDiff,LibertyThreadsActive
2020-03-18T18:18:26.872Z,libertymonServer,21799,6526,16011744,78093992,56,8,2.201171875,0.49799080664294193,0,0.0,669,0,815,0,1
2020-03-18T18:19:26.895Z,libertymonServer,21799,6534,17391336,71371240,56,8,3.13818359375,0.22420833333333334,0,0,675,6,821,6,0
2020-03-18T18:20:26.900Z,libertymonServer,21799,6534,16444488,71371240,56,8,1.947265625,0.21169274300932092,0,0,679,4,825,4,0
2020-03-18T18:21:26.902Z,libertymonServer,21799,6534,18126632,71371240,56,8,1.65380859375,0.2245086608927382,0.012815917883417266,0.0,682,3,828,3,0
```

## Development

Compile, package, and test all sub-projects:

`mvn install`

See each sub-project for detailed development.
