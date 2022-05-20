package no.difi.statistics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiInfo() {
        final String apiVersion = System.getProperty("difi.version", "N/A");
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Statistikk for offentlege tenester")
                                .description(
                                        "Skildring av API for uthenting av data (versjon %s).\n\n"
                                                + "<i>Tidsserie og måleavstand</i>\n"
                                                + "Ein tidsserie er identifisert av kombinasjonen av dei tre sti-parametera eigar, tidsserienamn og måleavstand. Ein tidsserie er lagra med ein spesifikk måleavstand (for eksempel 'hours'). Det er mogleg å konvertere til ein høgre måleavstand, til dømes frå 'hours' til 'months', i dei endepunkta med sti-parameteret 'targetDistance'. Moglege verdiar for måleavstand er 'minutes', 'hours', 'days', 'months', 'years'.\n\n"
                                                + "<i>Kategoriar</i>\n"
                                                + "Med parameteret 'categories' kan du filtrere data ut frå kategoriar oppgjevne på enkeltmålingane. For eksempel i statistikk for idporten-innloggingar, kan du hente ut datapunkt som kun tek med målingar der tenesteeigar er Skatteetaten.\n\n'"
                                                + "Merk at filteret ikkje filtrerer der det er nøyaktig lik verdi. Til dømes blir søketermen 'https://www.vest-testen.kommune.no/v3/' tolka til fleire søkeord (tokens): 'https', 'www.vest', 'testen.kommune.no' og 'v3'. Filteret sjekkar at alle søkeordene er med, men utelet ikkje treff som også har fleire søkeord. I dette dømet vert også 'https://www.vest-testen.kommune.no/sd/v3/' tatt med ('sd' er eit ekstra søkeord).\n\n"
                                                + "<i>Per kategorinøkkel</i>\n"
                                                + "Med parameteret 'perCategory' kan du hente ut datapunkt for kvar ulik verdi på kategorinøkkelen du oppgir. For eksempel, med statistikk for idporten-innloggingar, kan du få fleire datapunkt på samme tid, der kvart datapunkt er for ulike verdiar av Tjenesteeigar (kategorinøkkel). I dette eksempelet kan ulike verdiar av Tjenesteeigar kan vere Skatteetaten, Aure kommune osb.\n\n"
                                                + "<i>Tidspunkt</i>\n"
                                                + "Alle tidspunkt i parameter og responsar er oppgjevne i ISO 8601 datetime-format. Eksempel: '2018-06-18T09:00Z'."
                                )
                                .version(apiVersion)
                );
    }
}
