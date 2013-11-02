/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import dao.PagamentoJpaController;
import dao.exceptions.NonexistentEntityException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.persistence.Persistence;
import modelo.Pagamento;

/**
 *
 * @author Samia
 */
@ManagedBean
@RequestScoped
public class PagamentoMB {

    private Pagamento pg = new Pagamento();
    private PagamentoJpaController dao = new PagamentoJpaController(Persistence.createEntityManagerFactory("prova1JsfPU"));
    private String mensagem;
    public double positivo;
    public double negativo;
    public double balanco;
    private List<Pagamento> contas = new ArrayList<Pagamento>();

    /**
     * Creates a new instance of PagamentoMB
     */
    public PagamentoMB() {
    }

    public String inserir() {
        if (validarCamposEmBranco(getPg().getDescricao()) || validarCamposEmBranco(String.valueOf(getPg().getValor()))) {
            if (getPg().getValor() > 0) {
                try {
                    dao.create(getPg());
                    setMensagem("Pagamento inserido com sucesso");
                    return "index";
                } finally {
                }
            } else {
                setMensagem("Valor não pode ser negativo!");
                return "index";
            }
        } else {
            setMensagem("Campo obrigatorio!");
            return "index";
        }
    }

    public String alterar() throws NonexistentEntityException, Exception {
        if (validarCamposEmBranco(String.valueOf(getPg().getId()))) {
            if (getPg().getValor() > 0) {
                try {
                    dao.edit(getPg());
                    setMensagem("Pagamento alterado com sucesso");
                    return "index";
                } finally {
                }
            } else {
                setMensagem("Valor não pode ser negativo!");
                return "index";
            }
        } else {
            setMensagem("Campo obrigatório!");
            return "index";
        }
    }

    public String excluir() throws NonexistentEntityException {
        if (validarCamposEmBranco(String.valueOf(getPg().getId()))) {
            try {
                dao.destroy(getPg().getId());
                setMensagem("Pagamento excluido com sucesso");
                return "index";
            } finally {
            }
        } else {
            setMensagem("Campo obrigatório!");
            return "index";
        }
    }

    public void pesquisarTodos() {
        try {
            //
            setContas(dao.findPagamentoEntities());
        } finally {
        }
    }

    public String relatorio() {
        try {
            balanco = dao.balanco(getPg());
            negativo = dao.somaNegativa(getPg());
            positivo = dao.somaPositiva(getPg());
            setMensagem("Soma positiva: "+ positivo + "soma negativa: " + negativo + "balanco: " + balanco);
            return "index";            
        } finally {
        }
    }

    public boolean validarCamposEmBranco(String a) {
        if (a.equals("")) {
            return false;
        }
        return true;
    }

    /**
     * @return the pg
     */
    public Pagamento getPg() {
        return pg;
    }

    /**
     * @param pg the pg to set
     */
    public void setPg(Pagamento pg) {
        this.pg = pg;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    /**
     * @return the contas
     */
    public List<Pagamento> getContas() {
        return contas;
    }

    /**
     * @param contas the contas to set
     */
    public void setContas(List<Pagamento> contas) {
        this.contas = contas;
    }
}
