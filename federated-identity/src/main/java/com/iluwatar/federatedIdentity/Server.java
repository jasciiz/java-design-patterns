package com.iluwater.federatedIdentity;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.info.Info;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.util.*;

/**
 * provide function.
 * <p>
 * @author jasciiz
 */
public class Server {
    String name;
    ArrayList<String> IdPList;
    ArrayList<String> pass;
    Javalin app;
    static int index = 0;
    static String IdP = "http://localhost:7001/IdP";

    /**
     * construct without parameter.
     * <p>
     */
    public Server (){
        this.name = "Server1";
        this.app = Javalin.create(config -> {
            config.registerPlugin(getConfiguredOpenApiPlugin());
        }).start(7003);
        IdPList = new ArrayList<>();
        pass = new ArrayList<>();
    }

    /**
     * get IdP information.
     * <p>
     * @exception IOException throw
     * when IdP return nothing.
     */
    public boolean getIdP(){
        try {
            String IdP = Request.Get(this.IdP+"/getInfo").execute().returnContent().asString();
            IdPList.add(IdP);
            return true;
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * listen requests.
     * <p>
     * accept logIn, function.
     */
    public void listener(){
        this.app.post("/Server/logIn", ctx -> {
            ctx.res.setCharacterEncoding("UTF-8");
            String[] infos = ctx.body().split("//");
            if(IdPList.contains(infos[0])){
                pass.add(ctx.ip());
                ctx.result("success");
            }else {
                ctx.result("fail");
            }
        });

        this.app.get("/Server/function", ctx -> {
            ctx.res.setCharacterEncoding("UTF-8");
            if(pass.contains(ctx.ip())){
                ctx.result("function start");
            }else {
                ctx.result("illegal access");
            }
        });
    }

    /**
     * listen all http request.
     * <p>
     * accept register, getClaim, getInfo requests.
     */
    private OpenApiPlugin getConfiguredOpenApiPlugin() {
        Info info = new Info().version("1.0").description("RESTful Corpus Platform API");
        OpenApiOptions options = new OpenApiOptions(info)
                .activateAnnotationScanningFor("com.iluwater.dederatedIdentity.IdP")
                .path("/swagger-docs")
                .swagger(new SwaggerOptions("/swagger-ui"));
        return new OpenApiPlugin(options);
    }
}
