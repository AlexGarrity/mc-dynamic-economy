# Note - I very much doubt that this is the most efficient way to do this, but it works and it took 5 minutes to throw
# together.  A more modular solution would be nice (ie. throw any image files in and generate them at runtime, but
# that's (currently) out of the scope of the mod


import os
import re

base_file = """
{
  "parent": "item/generated",
  "textures": {
    "layer0": "dynamic_economy:item/${INTERNAL_TEXTURE}",
    "layer1": "dynamic_economy:item/${EXTERNAL_TEXTURE}"
  },
  "display": {
    "firstperson_lefthand": {
      "translation": [0, 1.5, 0],
      "scale": [0.25, 0.25, 0.25]
    },
    "firstperson_righthand": {
      "translation": [0, 1.5, 0],
      "scale": [0.25, 0.25, 0.25]
    },
    "thirdperson_lefthand": {
      "translation": [0, 1.5, 0],
      "scale": [0.25, 0.25, 0.25]
    },
    "thirdperson_righthand": {
      "translation": [0, 1.5, 0],
      "scale": [0.25, 0.25, 0.25]
    },
    "ground": {
      "translation": [0, 1, 0],
      "scale": [0.25, 0.25, 0.25]
    }
  }
}
"""


def main():
    internal_files = list()
    external_files = list()
    note_files = list()

    exterior_materials = list()
    interior_materials = list()
    note_materials = list()

    regex_capture = ".*_([a-z]+).png"
    path = r"../../textures/item"

    print(f"Looking for files in {path}")

    for root, dirs, files in os.walk(path):
        for file in files:
            if "coin_ext_" in file:
                external_files.append(file)
            elif "coin_int_" in file:
                internal_files.append(file)
            elif "note_" in file:
                note_files.append(file)

    print(f"\nInterior Files:")
    for internal_file in internal_files:
        match = re.match(regex_capture, internal_file)
        if match is None:
            continue
        interior_materials.append(match[1])
        print(f"\t{match[1]}")

    print(f"\nExterior Files:")
    for external_file in external_files:
        match = re.match(regex_capture, external_file)
        if match is None:
            continue
        exterior_materials.append(match[1])
        print(f"\t{match[1]}")

    print(f"\nNote Files:")
    for note_file in note_files:
        match = re.match(regex_capture, note_file)
        if match is None:
            continue
        note_materials.append(match[1])
        print(f"\t{match[1]}")

    print("")


    for exterior in exterior_materials:
        for interior in interior_materials:
            filename = f"coin_{exterior}_{interior}.json"
            with open(filename, "w") as file:
                file_to_write = base_file\
                    .replace("${EXTERNAL_TEXTURE}", f"coin_ext_{exterior}")\
                    .replace("${INTERNAL_TEXTURE}", f"coin_int_{interior}")
                file.write(file_to_write)
                print(f"Wrote file: \"{filename}\"")

    for note in note_materials:
        filename = f"note_{note}.json"
        with open(filename, "w") as file:
            file_to_write = base_file \
                .replace("${EXTERNAL_TEXTURE}", f"note_{note}") \
                .replace("${INTERNAL_TEXTURE}", f"note_{note}")
            file.write(file_to_write)
            print(f"Wrote file: \"{filename}\"")


if __name__ == "__main__":
    main()
