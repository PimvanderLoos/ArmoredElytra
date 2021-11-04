package nl.pim16aap2.armoredElytra.enchantmentcontainer;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class EnchantmentContainerAdapter extends TypeAdapter<EnchantmentContainer>
{
    @Override
    public void write(JsonWriter out, EnchantmentContainer enchantmentContainer)
        throws IOException
    {
        out.beginObject();
        out.name("size");
        out.value(enchantmentContainer.size());
        out.name("data");

        out.beginArray();
        for (final var entry : enchantmentContainer)
            writeEntry(out, entry);
        out.endArray();
        out.endObject();
    }

    private void writeEntry(JsonWriter out, Map.Entry<Enchantment, Integer> entry)
        throws IOException
    {
        out.beginObject();
        out.name("nmspc");
        out.value(entry.getKey().getKey().getNamespace());
        out.name("key");
        out.value(entry.getKey().getKey().getKey());
        out.name("lvl");
        out.value(entry.getValue());
        out.endObject();
    }

    private void readEnchantment(JsonReader in, Map<Enchantment, Integer> map)
        throws IOException
    {
        String namespace = null;
        String key = null;
        Integer level = null;

        in.beginObject();
        while (in.hasNext())
        {
            final String name = in.nextName();
            switch (name)
            {
                case "nmspc" -> namespace = in.nextString();
                case "key" -> key = in.nextString();
                case "lvl" -> level = in.nextInt();
            }
        }

        Objects.requireNonNull(namespace, "Namespace cannot be null!");
        Objects.requireNonNull(key, "Key cannot be null!");
        Objects.requireNonNull(level, "Level cannot be null!");

        //noinspection deprecation
        final NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
        map.put(Enchantment.getByKey(namespacedKey), level);
        in.endObject();
    }

    @Override
    public EnchantmentContainer read(JsonReader in)
        throws IOException
    {
        in.beginObject();
        in.skipValue();

        final int arrSize = in.nextInt();
        if (arrSize == 0)
            return new EnchantmentContainer();

        final Map<Enchantment, Integer> enchantments = new LinkedHashMap<>(arrSize);

        in.skipValue();
        in.beginArray();
        while (in.hasNext())
            readEnchantment(in, enchantments);
        in.endArray();
        in.endObject();

        return new EnchantmentContainer(enchantments);
    }
}
