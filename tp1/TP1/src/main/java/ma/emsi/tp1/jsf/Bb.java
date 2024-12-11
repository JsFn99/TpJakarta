package ma.emsi.tp1.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.tp1.llm.JsonUtilPourGemini;
import ma.emsi.tp1.llm.LlmInteraction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Backing bean pour la gestion de la page JSF.
 * Portée view pour conserver l'état de la conversation entre plusieurs requêtes HTTP.
 */
@Named
@ViewScoped
public class Bb implements Serializable {

    /**
     * Rôle "système" attribué à un LLM.
     * Peut être modifié par l'utilisateur via l'interface.
     * De nouveaux rôles peuvent être ajoutés dans la méthode getSystemRoles.
     */
    private String systemRole = "helpful assistant";

    /**
     * Indique si le rôle système peut encore être modifié.
     */
    private boolean systemRoleChangeable = true;

    /**
     * Dernière question posée par l'utilisateur.
     */
    private String question;

    /**
     * Dernière réponse reçue de l'API LLM.
     */
    private String reponse;

    /**
     * Historique complet de la conversation.
     */
    private StringBuilder conversation = new StringBuilder();

    /**
     * JSON de la requête envoyée.
     */
    private String texteRequeteJson;

    /**
     * JSON de la réponse reçue.
     */
    private String texteReponseJson;

    /**
     * Mode debug pour afficher les détails techniques.
     */
    private boolean debug;

    // Getters et setters pour les nouvelles propriétés
    public String getTexteRequeteJson() {
        return texteRequeteJson;
    }

    public void setTexteRequeteJson(String texteRequeteJson) {
        this.texteRequeteJson = texteRequeteJson;
    }

    public String getTexteReponseJson() {
        return texteReponseJson;
    }

    public void setTexteReponseJson(String texteReponseJson) {
        this.texteReponseJson = texteReponseJson;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    // Méthode pour basculer le mode debug
    public String toggleDebug() {
        this.debug = !this.debug;
        return null; // Rester sur la même page
    }

    @Inject
    private FacesContext facesContext;

    /**
     * Constructeur par défaut requis pour un bean CDI.
     */
    public Bb() {
    }

    public String getSystemRole() {
        return systemRole;
    }

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
    }

    public boolean isSystemRoleChangeable() {
        return systemRoleChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    /**
     * Setter utilisé pour l'affichage de la réponse dans le textarea.
     *
     * @param reponse la réponse obtenue.
     */
    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    /**
     * Envoie une question au serveur et traite la réponse.
     * Le serveur effectue un traitement simple en attendant d'intégrer une interaction complète avec un LLM.
     *
     * @return null pour rester sur la même page.
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Veuillez saisir une question.");
            facesContext.addMessage(null, message);
            return null;
        }
        try {
            // Appel à JsonUtil pour gérer l'interaction avec le LLM
            JsonUtilPourGemini jsonUtil = new JsonUtilPourGemini();
            LlmInteraction interaction = jsonUtil.envoyerRequete(question);
            this.reponse = interaction.texteReponse();
            this.texteRequeteJson = interaction.texteRequeteJson();
            this.texteReponseJson = interaction.texteReponseJson();
        } catch (Exception e) {
            e.printStackTrace();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Problème de connexion avec l'API du LLM",
                    "Erreur rencontrée : " + e.getMessage());
            facesContext.addMessage(null, message);
            return null;
        }
        // Mise à jour de l'historique de la conversation
        if (this.conversation.isEmpty()) {
            this.reponse = systemRole.toUpperCase(Locale.FRENCH) + "\n" + this.reponse;
            this.systemRoleChangeable = false;
        }
        afficherConversation();
        return null;
    }

    /**
     * Réinitialise le chat en redirigeant vers la page index.xhtml.
     * Cette opération crée une nouvelle instance du backing bean.
     *
     * @return "index" pour recharger la vue.
     */
    public String nouveauChat() {
        return "index";
    }

    /**
     * Met à jour l'affichage de la conversation dans le textarea.
     */
    private void afficherConversation() {
        this.conversation.append("* Utilisateur:\n").append(question).append("\n* Serveur:\n").append(reponse).append("\n");
    }

    /**
     * Fournit une liste de rôles système disponibles pour l'interaction avec le LLM.
     *
     * @return Liste des rôles sous forme d'objets SelectItem.
     */
    public List<SelectItem> getSystemRoles() {
        List<SelectItem> listeSystemRoles = new ArrayList<>();
        // Rôle pour la traduction anglais-français
        String role = """
                Vous êtes un interprète. Vous traduisez de l'anglais vers le français et inversement.
                Si l'utilisateur saisit un texte en français, vous le traduisez en anglais.
                Si le texte est en anglais, vous le traduisez en français.
                Pour un texte de un à trois mots, fournissez des exemples d'utilisation.
                """;
        listeSystemRoles.add(new SelectItem(role, "Traducteur Anglais-Français"));

        // Rôle pour la suggestion de lieux touristiques
        role = """
                Vous êtes un guide touristique. Si l'utilisateur saisit le nom d'un pays ou d'une ville,
                vous indiquez les principaux lieux à visiter et le prix moyen d'un repas.
                """;
        listeSystemRoles.add(new SelectItem(role, "Guide touristique"));

        return listeSystemRoles;
    }
}
