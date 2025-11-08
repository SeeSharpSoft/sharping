package lexerdef;

import net.seesharpsoft.commons.util.Lexer;
import net.seesharpsoft.commons.util.SharpIO;
import net.seesharpsoft.commons.util.Tokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class CsvLexerTest {

    private Lexer csvLexer;

    @BeforeEach
    public void beforeEach() throws IOException {
        csvLexer = new Lexer<>(new Tokenizer<>());

        csvLexer.init("/lexerdef/csv.lex");
    }


    @Test
    public void should_lex_simple_csv() throws IOException, ParseException {
        List<Lexer.StateInfo> stateInfo = csvLexer.tokenize(SharpIO.readAsString("/lexerdef/csv/simple.csv"));

        assertThat(stateInfo, hasSize(7));
    }

    @Test
    public void should_lex_complex_csv() throws IOException, ParseException {
        List<Lexer.StateInfo> stateInfo = csvLexer.tokenize(SharpIO.readAsString("/lexerdef/csv/complex.csv"));

        assertThat(stateInfo, hasSize(68));
    }

    @Test
    public void should_lex_large_csv() throws IOException, ParseException {
        long start = System.currentTimeMillis();
        List<Lexer.StateInfo> stateInfo = csvLexer.tokenize(SharpIO.readAsString("/lexerdef/csv/large.csv"));
        long end = System.currentTimeMillis();

        System.out.println("Elapsed time in s: " + ((end - start) / 1000.0));

        assertThat(stateInfo, hasSize(27000));
    }

    @Test
    public void should_lex_huge_csv() throws IOException, ParseException {
        long start = System.currentTimeMillis();
        List<Lexer.StateInfo> stateInfo = csvLexer.tokenize(SharpIO.readAsString("/lexerdef/csv/huge.csv"));
        long end = System.currentTimeMillis();

        System.out.println("Elapsed time in s: " + ((end - start) / 1000.0));

        assertThat(stateInfo, hasSize(136529));
    }
}
