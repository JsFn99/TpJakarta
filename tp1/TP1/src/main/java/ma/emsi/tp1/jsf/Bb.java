package ma.emsi.tp1.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Backing bean pour la page JSF index.xhtml.
 * Portée view pour conserver l'état de la conversation pendant plusieurs requêtes HTTP.
 */
@Named
@ViewScoped
public class Bb implements Serializable {

    /**
     * Rôle "système" que l'on attribuera plus tard à un LLM.
     * Valeur par défaut que l'utilisateur peut modifier.
     * Possible d'ajouter de nouveaux rôles dans la méthode getSystemRoles.
     */
    private String systemRole = "helpful assistant";

    /**
     * Quand le rôle est choisi par l'utilisateur dans la liste déroulante,
     * il n'est plus possible de le modifier (voir code de la page JSF).
     */
    private boolean systemRoleChangeable = true;

    /**
     * Dernière question posée par l'utilisateur.
     */
    private String question;

    /**
     * Dernière réponse du serveur.
     */
    private String reponse;

    /**
     * La conversation depuis le début.
     */
    private StringBuilder conversation = new StringBuilder();

    /**
     * Contexte JSF. Utilisé pour qu'un message d'erreur s'affiche dans le formulaire.
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Obligatoire pour un bean CDI (classe gérée par CDI).
     */
    public Bb() {
    }

    // Getters et setters
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
     * Envoie la question au serveur.
     * Traitement : met en évidence les mots longs (plus de 6 caractères) en les entourant d'astérisques.
     *
     * @return null pour rester sur la même page.
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        // Initialisation de la réponse
        this.reponse = "";
        this.systemRoleChangeable = false;

        // Traitement : trouver les mots longs (plus de 6 caractères)
        String[] tokens = question.split(" ");
        List<String> motsLongs = new ArrayList<>();

        for (String token : tokens) {
            if (token.length() > 6) {
                motsLongs.add("***" + token.toUpperCase(Locale.FRENCH) + "***");
            }
        }

        // Ajouter le rôle de l'API en début de réponse
        this.reponse += "Rôle : " + systemRole.toUpperCase(Locale.FRENCH) + "\n";

        // Ajouter les mots longs
        if (!motsLongs.isEmpty()) {
            this.reponse += "Mots longs détectés :\n" + String.join(" ", motsLongs) + "\n";
            this.reponse += "Total : " + motsLongs.size() + " mot(s) long(s).\n";
        } else {
            this.reponse += "Aucun mot long détecté dans votre question.\n";
        }

        // Afficher la conversation dans l'historique
        afficherConversation();
        return null;
    }

    /**
     * Pour un nouveau chat.
     * @return "index" pour recommencer une nouvelle conversation.
     */
    public String nouveauChat() {
        return "index";
    }

    /**
     * Pour afficher la conversation dans le textArea de la page JSF.
     */
    private void afficherConversation() {
        this.conversation.append("* Utilisateur:\n").append(question)
                .append("\n* Serveur:\n").append(reponse).append("\n");
    }

    /**
     * Retourne la liste des rôles systèmes disponibles.
     * @return liste des rôles systèmes.
     */
    public List<SelectItem> getSystemRoles() {
        List<SelectItem> listeSystemRoles = new ArrayList<>();
        String role = """
                You are an interpreter. You translate from English to French and from French to English.
                If the user type a French text, you translate it into English.
                If the user type an English text, you translate it into French.
                If the text contains only one to three words, give some examples of usage of these words in English.
                """;
        listeSystemRoles.add(new SelectItem(role, "Traducteur Anglais-Français"));
        role = """
                You are a travel guide. If the user types the name of a country or town,
                you tell them the main places to visit and the average price of a meal.
                """;
        listeSystemRoles.add(new SelectItem(role, "Guide touristique"));
        return listeSystemRoles;
    }
}
