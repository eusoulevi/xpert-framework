#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.handle;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

/**
 * ExceptionHandlerFactory para gerenciar os erros nao tratados
 * 
 * @see ApplicationExceptionHandler
 * @author ayslan
 */
public class ApplicationExceptionHandlerFactory extends ExceptionHandlerFactory{

    private ExceptionHandlerFactory parent;

    public ApplicationExceptionHandlerFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        ExceptionHandler result =
                new ApplicationExceptionHandler(parent.getExceptionHandler());
        return result;
    }
}
