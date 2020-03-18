# libertymon

## Usage

* Download libertymon.war from https://github.com/kgibm/libertymon/releases
* Deploy libertymon.war to Liberty
* Add `<webContainer deferServletLoad="false" />` to server.xml.
* Statistics file automatically written to `${Liberty}/usr/servers/${SERVER}/logs/libertymon.csv` (or wherever LOG_DIR, WLP_OUTPUT_DIR, or -DLIBERTYMON_DIR point to).

## Development

* `mvn liberty:dev`
