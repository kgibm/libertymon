# libertymon

## Usage

* Download libertymon.war from https://github.com/kgibm/libertymon/releases
* Deploy libertymon.war to Liberty
* Add `<featureManager><feature>monitor-1.0</feature></featureManager>` to server.xml.
* Add `<webContainer deferServletLoad="false" />` to server.xml.
* Statistics file automatically written to `${Liberty}/usr/servers/${SERVER}/logs/libertymon.csv` (or wherever LOG_DIR, WLP_OUTPUT_DIR, or -DLIBERTYMON_DIR point to).

## Example

```
Time,Name,PID,Classes,JavaHeap,JVMHeap,TotalThreads,CPUThreads,SystemLoadAverage1Min,ProcessCPUCumulative,ProcessCPUDiff,ProcessCPU%,GCsCumulative,GCsDiff,GCTimeCumulative,GCTimeDiff,LibertyThreadsActive
2020-03-18T18:18:26.872Z,libertymonServer,21799,6526,16011744,78093992,56,8,2.201171875,0.49799080664294193,0,0.0,669,0,815,0,1
2020-03-18T18:19:26.895Z,libertymonServer,21799,6534,17391336,71371240,56,8,3.13818359375,0.22420833333333334,0,0,675,6,821,6,0
2020-03-18T18:20:26.900Z,libertymonServer,21799,6534,16444488,71371240,56,8,1.947265625,0.21169274300932092,0,0,679,4,825,4,0
2020-03-18T18:21:26.902Z,libertymonServer,21799,6534,18126632,71371240,56,8,1.65380859375,0.2245086608927382,0.012815917883417266,0.0,682,3,828,3,0
```

## Development

* `mvn liberty:dev`
