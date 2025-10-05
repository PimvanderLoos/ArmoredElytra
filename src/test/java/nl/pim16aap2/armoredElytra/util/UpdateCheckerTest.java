package nl.pim16aap2.armoredElytra.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.semver4j.Semver;

import java.util.stream.Stream;

class UpdateCheckerTest
{
    @ParameterizedTest
    @MethodSource("versionComparisonDataStream")
    void snapshot_test(VersionData data)
    {
        Assertions.assertEquals(data.output, UpdateChecker.parseVersion(data.input));
    }

    private static Stream<VersionData> versionComparisonDataStream()
    {
        return Stream.of(
            new VersionData(
                "1.2",
                Semver.of().withMajor(1).withMinor(2).withPatch(0).toSemver()),
            new VersionData(
                "1.2.0-SNAPSHOT",
                Semver.of().withMajor(1).withMinor(2).withPatch(0).withPreRelease("SNAPSHOT").toSemver())
        );
    }

    private record VersionData(String input, Semver output)
    {
    }
}
