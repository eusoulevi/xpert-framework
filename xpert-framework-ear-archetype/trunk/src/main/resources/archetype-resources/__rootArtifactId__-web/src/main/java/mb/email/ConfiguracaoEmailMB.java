#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.mb.email;

import ${package}.bo.email.ConfiguracaoEmailBO;
import ${package}.bo.email.EmailBO;
import ${package}.modelo.email.ConfiguracaoEmail;
import java.io.Serializable;
import com.xpert.core.crud.AbstractBaseBean;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import com.xpert.core.exception.BusinessException;
import com.xpert.faces.utils.FacesMessageUtils;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author ayslan
 */
@ManagedBean
@ViewScoped
public class ConfiguracaoEmailMB extends AbstractBaseBean<ConfiguracaoEmail> implements Serializable {
    
    @EJB
    private ConfiguracaoEmailBO configuracaoEmailBO;
    @EJB
    private EmailBO emailBO;
    
    @NotBlank(message="required.mensagem")
    private String mensagem;
    
    @NotBlank(message="required.email")
    @Email(message="business.emailInvalido")
    private String email;
    
    public void enviarEmail() {
        try {
            emailBO.enviar("Mensagem de Teste", mensagem, getEntity(), email);
            FacesMessageUtils.sucess();
        } catch (BusinessException ex) {
            FacesMessageUtils.error(ex);
        }
    }
    
    @Override
    public ConfiguracaoEmailBO getBO() {
        return configuracaoEmailBO;
    }
    
    @Override
    public String getDataModelOrder() {
        return "email";
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    
}
