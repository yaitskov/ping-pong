function ellipsis(s) {
   return s.replace('...',
                    '<span class="glyphicon glyphicon-refresh spinning-fast"/>');
}

function applyAllToString(s) {
   return ellipsis(s);
}

export default function applyAllToMap(m) {
   Object.keys(m).map((key) => {
      m[key] = applyAllToString(m[key]);
   });
   return m;
}

