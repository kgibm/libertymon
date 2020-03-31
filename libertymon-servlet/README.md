# libertymon-servlet

Liberty WAR file that periodically writes statistics about the Liberty server to a CSV file (e.g. `logs/libertymon.csv`).

Requires Liberty features jsp-2.3 and monitor-1.0; however, in addition, it also requires `<webContainer deferServletLoad="false" />`.

## Usage

* Download libertymon-servlet.war from https://github.com/kgibm/libertymon/releases
* Deploy libertymon-servlet.war to Liberty (e.g. dropins)
* Add `<featureManager><feature>monitor-1.0</feature><feature>jsp-2.3</feature></featureManager>` to server.xml if not already included.
* Add `<webContainer deferServletLoad="false" />` to server.xml if not already included.
* Statistics file automatically written to `${Liberty}/usr/servers/${SERVER}/logs/libertymon.csv` (or wherever `LOG_DIR`, `WLP_OUTPUT_DIR`, or `-DLIBERTYMON_DIR` point to).

## Example

```
Time,Name,PID,Classes,JavaHeap,JVMHeap,TotalThreads,CPUThreads,SystemLoadAverage1Min,ProcessCPUCumulative,ProcessCPUDiff,ProcessCPU%,GCsCumulative,GCsDiff,GCTimeCumulative,GCTimeDiff,LibertyThreadsActive
2020-03-18T18:18:26.872Z,libertymonServer,21799,6526,16011744,78093992,56,8,2.201171875,0.49799080664294193,0,0.0,669,0,815,0,1
2020-03-18T18:19:26.895Z,libertymonServer,21799,6534,17391336,71371240,56,8,3.13818359375,0.22420833333333334,0,0,675,6,821,6,0
2020-03-18T18:20:26.900Z,libertymonServer,21799,6534,16444488,71371240,56,8,1.947265625,0.21169274300932092,0,0,679,4,825,4,0
2020-03-18T18:21:26.902Z,libertymonServer,21799,6534,18126632,71371240,56,8,1.65380859375,0.2245086608927382,0.012815917883417266,0.0,682,3,828,3,0
```

## Development

### Maven goals

`mvn liberty:dev`

Other useful goals:

* `mvn install`: Compile, package, and test.
* `mvn liberty:run`: Run Liberty in the foreground.
* `mvn liberty:start`: Start Liberty in the background.
* `mvn liberty:stop`: Stop background Liberty.
* `mvn clean`: Delete all built resources.
* `mvn compile`: Compile all projects.
* `mvn package`: Package all projects (e.g. war).

### Logs

`$ cat target/liberty/wlp/usr/servers/libertymonServer/logs/libertymon.csv`
