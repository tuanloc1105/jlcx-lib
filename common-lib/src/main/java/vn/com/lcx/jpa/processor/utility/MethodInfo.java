package vn.com.lcx.jpa.processor.utility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MethodInfo {
    private String methodName;
    private List<? extends VariableElement> inputParameters;
    private TypeMirror outputParameter;
}
