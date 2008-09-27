package org.beynet.utils.webtools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Cette annotation doit etre posee pour tout objet
 * que l'on voudra utiliser dans un object FormSelect
 * @author beynet
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FormSelectAnnotation {
	/**
	 * Donne la methode a appeler pour recuperer le texte de la balise option
	 * @return
	 */
	String getText();
	/**
	 * permet de positionner la methode retournant la valeur de l'attribut value
	 * @return
	 */
	String getValue();
	/**
	 * methode utilise pour ordonnancer deux options
	 * @return
	 */
	String compareToMethodName()  default ""; 
}
