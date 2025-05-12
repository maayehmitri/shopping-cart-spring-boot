#include <map>
#include <utility>
#include <type_traits>

template<typename K, typename V>
class interval_map {
    friend void IntervalMapTest();
    V m_valBegin;
    std::map<K, V> m_map;
public:
    // Constructor associates the whole range of K with val
    template<typename V_forward>
    interval_map(V_forward&& val)
    : m_valBegin(std::forward<V_forward>(val))
    {}

    // Assign value val to interval [keyBegin, keyEnd).
    // Overwrite previous values in this interval.
    template<typename V_forward>
    void assign(K const& keyBegin, K const& keyEnd, V_forward&& val)
        requires (std::is_same<std::remove_cvref_t<V_forward>, V>::value)
    {
        if (!(keyBegin < keyEnd)) {
            // Empty interval, do nothing
            return;
        }

        // Find the position to insert or update the beginning of the interval
        auto beginIt = m_map.lower_bound(keyBegin);
        auto endIt = m_map.lower_bound(keyEnd);

        // If the value at the beginning is already val, no need to add a new entry
        if (beginIt != m_map.begin() && std::prev(beginIt)->second == val) {
            beginIt = m_map.erase(beginIt, endIt);
        } else {
            beginIt = m_map.erase(beginIt, endIt);
            m_map[keyBegin] = std::forward<V_forward>(val);
        }

        // Check if we need to restore the value after the interval
        if (endIt != m_map.end() && endIt->first == keyEnd) {
            return;
        }
        if (endIt == m_map.end() || endIt->second != val) {
            m_map[keyEnd] = (endIt == m_map.end()) ? m_valBegin : endIt->second;
        }
    }

    // Look-up of the value associated with key
    V const& operator[](K const& key) const {
        auto it = m_map.upper_bound(key);
        if (it == m_map.begin()) {
            return m_valBegin;
        } else {
            return std::prev(it)->second;
        }
    }
};
