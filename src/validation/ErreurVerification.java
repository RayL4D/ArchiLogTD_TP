package validation;

/**
 * Représente une erreur de validation détectée lors des vérifications des entités du projet.
 * Cette classe encapsule le code de l'erreur, le contexte dans lequel elle s'est produite,
 * ainsi qu'un message descriptif complet.
 */
public class ErreurVerification {

  private CodeErreur code;
  private ContexteErreur contexte;
  private String message;

  /**
   * Construit une erreur de vérification avec le message par défaut associé au code d'erreur.
   *
   * @param code le code spécifique de l'erreur rencontrée
   * @param contexte le contexte d'exécution dans lequel l'erreur s'est produite
   */
  public ErreurVerification(CodeErreur code, ContexteErreur contexte) {
    this.code = code;
    this.contexte = contexte;
    this.message = code.getMessageParDefaut();
  }

  /**
   * Construit une erreur de vérification en ajoutant un détail spécifique au message par défaut.
   *
   * @param code le code spécifique de l'erreur rencontrée
   * @param contexte le contexte d'exécution dans lequel l'erreur s'est produite
   * @param detail une information supplémentaire pour préciser l'erreur (ex: le nom de 
   *     la variable en cause)
   */
  public ErreurVerification(CodeErreur code, ContexteErreur contexte, String detail) {
    this.code = code;
    this.contexte = contexte;
    this.message = code.getMessageParDefaut() + " (" + detail + ")";
  }

  /**
   * Récupère le code de l'erreur.
   *
   * @return le code d'erreur
   */
  public CodeErreur getCode() {
    return code;
  }

  /**
   * Récupère le contexte dans lequel l'erreur a été levée.
   *
   * @return le contexte de l'erreur
   */
  public ContexteErreur getContexte() {
    return contexte;
  }

  /**
   * Récupère le message descriptif de l'erreur, incluant les potentiels détails.
   *
   * @return le message de l'erreur
   */
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "ErreurVerification [code = "
        + code
        + ", contexte = "
        + contexte
        + ", message = "
        + message
        + "]";
  }
}