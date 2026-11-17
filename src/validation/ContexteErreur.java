package validation;

/**
 * Énumération définissant les différents contextes d'exécution dans lesquels 
 * une erreur de validation peut survenir au sein de l'application.
 */
public enum ContexteErreur {
  CREATION_PROJET,
  VERIFICATION_PROJET,
  FACADE_GESTION
}