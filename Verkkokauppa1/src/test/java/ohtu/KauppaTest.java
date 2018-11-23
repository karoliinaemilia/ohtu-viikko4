package ohtu;

import ohtu.verkkokauppa.Kauppa;
import ohtu.verkkokauppa.Pankki;
import ohtu.verkkokauppa.Tuote;
import ohtu.verkkokauppa.Varasto;
import ohtu.verkkokauppa.Viitegeneraattori;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class KauppaTest {

    Pankki pankki = mock(Pankki.class);
    Viitegeneraattori viite;
    Varasto varasto;
    Kauppa k;

    @Before
    public void setUp() {
        viite = mock(Viitegeneraattori.class);
        varasto = mock(Varasto.class);
        k = new Kauppa(varasto, pankki, viite);
    }

    @Test
    public void ostoksenPaatyttyaPankinMetodiaTilisiirtoKutsutaanOikeillaArvoilleKunOstetaanYksiTuote() {
        when(viite.uusi()).thenReturn(134);
        when(varasto.saldo(1)).thenReturn(100);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "Koff Portteri", 3));

        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.tilimaksu("pekka", "12345");

        verify(pankki).tilisiirto("pekka", 134, "12345", "33333-44455", 3);

    }

    @Test
    public void ostoksenPaatyttyaPankinMetodiaTilisiirtoKutsutaanOikeillaArvoillaKunOstetaanKaksiEriTuotetta() {
        when(viite.uusi()).thenReturn(12);
        when(varasto.saldo(2)).thenReturn(25);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "Fink Bräu I", 1));

        when(varasto.saldo(4)).thenReturn(1);
        when(varasto.haeTuote(4)).thenReturn(new Tuote(5, "Expensive Beer", 456));

        k.aloitaAsiointi();
        k.lisaaKoriin(2);
        k.lisaaKoriin(4);
        k.tilimaksu("jarno", "34567");

        verify(pankki).tilisiirto("jarno", 12, "34567", "33333-44455", 457);
    }

    @Test
    public void ostoksenPaatyttyaPankinMetodiaTilisiirtoKutsutaanOikeillaArvoillaKunOstetaanKaksiSamaaTuotetta() {
        when(viite.uusi()).thenReturn(122);
        when(varasto.saldo(2)).thenReturn(25);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "Fink Bräu I", 1));

        k.aloitaAsiointi();
        k.lisaaKoriin(2);
        k.lisaaKoriin(2);
        k.tilimaksu("erno", "3467");

        verify(pankki).tilisiirto("erno", 122, "3467", "33333-44455", 2);
    }

    @Test
    public void ostoksenPaatyttyaPankinMetodiaTilisiirtoKutsutaanOikeillaArvoillaKunYksiOstetavistaTuotteistaOnLoppu() {
        when(viite.uusi()).thenReturn(22);
        when(varasto.saldo(2)).thenReturn(25);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "Fink Bräu I", 1));

        when(varasto.saldo(4)).thenReturn(0);
        when(varasto.haeTuote(4)).thenReturn(null);

        k.aloitaAsiointi();
        k.lisaaKoriin(2);
        k.lisaaKoriin(4);
        k.tilimaksu("jaakko", "34");

        verify(pankki).tilisiirto("jaakko", 22, "34", "33333-44455", 1);
    }

    @Test
    public void aloitaAsiointiKutsuminenNollaaEdellisenOstoksenTiedot() {
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "Koff Portteri", 3));
        when(varasto.saldo(1)).thenReturn(25);
        k.aloitaAsiointi();
        
        k.lisaaKoriin(1);
        k.lisaaKoriin(1);
        
        k.tilimaksu("jarkko", "90");
        
        verify(pankki).tilisiirto(eq("jarkko"), anyInt(), anyString(), anyString(), eq(6));
        
        k.aloitaAsiointi();
        k.tilimaksu("arno", "98");
        
        verify(pankki).tilisiirto(eq("arno"), anyInt(), anyString(), anyString(), eq(0));

    }

    @Test
    public void kauppaPyytaaAinaUudenViitenumeron() {
        k.aloitaAsiointi();
        
        k.lisaaKoriin(2);
        k.lisaaKoriin(4);
        
        k.tilimaksu("pekka", "78");
        
        verify(viite, times(1)).uusi();
        
        k.lisaaKoriin(2);
        k.lisaaKoriin(4);

        k.tilimaksu("anna", "908");

        verify(viite, times(2)).uusi();
    }
    
    @Test
    public void ostosoristaPoistaminenOnnistuu() {
        when(viite.uusi()).thenReturn(122);
        when(varasto.saldo(2)).thenReturn(25);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "Fink Bräu I", 1));

        k.aloitaAsiointi();
        k.lisaaKoriin(2);
        k.lisaaKoriin(2);
        k.poistaKorista(2);
        k.tilimaksu("erno", "3467");

        verify(pankki).tilisiirto("erno", 122, "3467", "33333-44455", 1);
    }
}
