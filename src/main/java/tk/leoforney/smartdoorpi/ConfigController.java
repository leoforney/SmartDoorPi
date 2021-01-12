package tk.leoforney.smartdoorpi;

import com.google.gson.Gson;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {

    @GetMapping("/getConfig")
    public String getConfig() {
        Gson gson = new Gson();
        return gson.toJson(Options.grabConfig().doors);
    }

}
