package io.rocktest;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RockException extends RuntimeException {

    Step step=null;
    String module=null;
    String scenario=null;
    List<String> stack=null;
    int stepNumber=0;

    public RockException(String message){
        super(message);
    }

    public RockException(String message,Throwable t){
        super(message,t);
    }

    public RockException(Throwable t){
        super("RockException", t);
    }

    public String getDescription() {
        StringBuilder sb=new StringBuilder();

        if(getMessage()!=null) {
            sb.append("message: ");
            sb.append(getMessage());
            sb.append("\n");
        }

        if(step!=null)
            sb.append(step.toYaml("step"));

        if(module != null) {
            sb.append("module: ");
            sb.append(module);
            sb.append("\n");
        }

        sb.append("stepNumber: ");
        sb.append(stepNumber);
        sb.append("\n");

        if(scenario!=null) {
            sb.append("scenario: ");
            sb.append(scenario);
            sb.append("\n");
        }

        if(stack!=null) {
            sb.append("stack:\n");
            for (String s : stack) {
                sb.append("  - ");
                sb.append(s);
                sb.append("\n");
            }
        }

        if(getCause()!=null) {
            sb.append("cause:\n");
            sb.append("  type: ");
            sb.append(getCause().getClass().getName());
            sb.append("\n");
            if(getCause().getMessage()!=null) {
                sb.append("  message: ");
                sb.append(getCause().getMessage());
                sb.append("\n");
            }
        }

        return sb.toString();
    }

}
