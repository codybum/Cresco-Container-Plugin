import com.google.auto.service.AutoService;
import com.researchworx.cresco.library.plugin.core.CPlugin;

import java.util.ArrayList;
import java.util.List;

@AutoService(CPlugin.class)
public class Plugin extends CPlugin {

    public DockerEngine de;
    private PerfMonitor perfMonitor;
//


    @Override
    public void setExecutor() {
        setExec(new Executor(this));
    }

    public void start() {

        de = new DockerEngine();

        String containerImage = this.config.getStringParam("container_image");
        if(containerImage == null) {
            logger.error("start() Container must privite image name!");
        }
        else {
            List<String> envList = parseEParams(this.config.getStringParam("e_params"));
            if(envList != null) {
                for (String ep : envList) {
                    logger.info("e_param: " + ep);
                }
            }
            List<String> portList = parsePParams(this.config.getStringParam("p_params"));
            if(portList != null) {
                for (String p : portList) {
                    logger.info("p_param: " + p);
                }
            }
            String container_id = de.createContainer(containerImage,envList,portList);
            de.startContainer(container_id);
            logger.info("Container initialized");

            perfMonitor = new PerfMonitor(this, de, container_id);
            perfMonitor.start();
            logger.info("Container performance monitoring initialized");

            setExec(new Executor(this));
        }


    }


    private List<String> parsePParams(String paramString) {
        List<String> params = null;
        try {
            if(paramString != null) {
                params = new ArrayList<>();
                if(paramString.contains(":")){
                    for(String param : paramString.split(":")) {
                        params.add(param);
                    }
                }
                else {
                    params.add(paramString);
                }
            }
        }
        catch(Exception ex) {
            logger.error("parseParams " + ex.getMessage());
        }

        return params;
    }

    private List<String> parseEParams(String paramString) {
        List<String> params = null;
        try {
            if(paramString != null) {
                params = new ArrayList<>();
                if(paramString.contains(":")){
                    for(String param : paramString.split(":")) {
                        String paramVal = this.config.getStringParam(param);
                        if(paramVal != null) {
                            params.add(param + "=" + paramVal);
                        }
                    }
                }
                else {
                    String paramVal = this.config.getStringParam(paramString);
                    if(paramVal != null) {
                        params.add(paramString + "=" + paramVal);
                    }
                }
            }
        }
        catch(Exception ex) {
            logger.error("parseParams " + ex.getMessage());
        }

        return params;
    }

    @Override
    public void cleanUp() {

        perfMonitor.stop();
        de.shutdown();

    }
}
